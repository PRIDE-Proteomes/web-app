package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithPeptiforms;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.*;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.GroupRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.PeptiformRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.ProteinRetriever;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;
import uk.ac.ebi.pride.proteomes.web.client.utils.Triplet;

import java.util.*;

/**
 * This class is the one responsible of retrieving data using retrievers and
 * caching the data to speed up the application.
 * Because we assume that the code can be interrupted while doing the
 * requests, we use a list of batched requests per type of data requested.
 * This allows us to handle more than one concurrent request without affecting
 * the others.
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:10
 */
public class DataProvider implements DataServer, TransactionHandler {
    private DataServer.DataClient client = null;

    private Map<String, Integer> speciesCache = new HashMap<>();

    private Map<String, Group> groupCache = new HashMap<>();
    private List<Map<String, Boolean>> groupRequests = new ArrayList<>();
    private final GroupRetriever groupRetriever;

    private Map<String, Protein> proteinCache = new HashMap<>();
    private List<Map<String, Boolean>> proteinRequests = new ArrayList<>();
    private final ProteinRetriever proteinRetriever;

    // We cache the PeptideLists retrieved as well as store the mapping between
    // sequence and peptide matches, this way we can build PeptideWithPeptiforms
    // dynamically while avoid being a time hog.
    private Map<String, PeptideList> peptiformListCache = new HashMap<>();
    // the pair represents <peptide sequence, protein accession>
    private Map<Pair<String, String>, List<PeptideMatch>> peptideMatchCache = new HashMap<>();
    //the Map is <peptide sequence, protein acce>
    private List<Map<Triplet<String, Integer, String>, Boolean>> peptiformRequests = new ArrayList<>();
    private final PeptiformRetriever peptiformRetriever;

    private Map<String, Peptide> peptiformCache = new HashMap<>();

    public DataProvider(String webServiceRoot) {
        groupRetriever = new GroupRetriever(webServiceRoot);
        groupRetriever.addHandler(this);

        proteinRetriever = new ProteinRetriever(webServiceRoot);
        proteinRetriever.addHandler(this);

        peptiformRetriever = new PeptiformRetriever(webServiceRoot);
        peptiformRetriever.addHandler(this);
    }

    @Override
    public void onTransactionFinished(Transaction transaction) {
        if(transaction.getResponse() instanceof Group) {
            Group group = (Group) transaction.getResponse();
            groupCache.put(group.getId(), group);

            for(Map<String, Boolean> batchRequest : groupRequests) {
                if(batchRequest.containsKey(group.getId())) {
                    batchRequest.put(group.getId(), true);
                }
            }

            // If there are any request that have been processed completely we
            // should return the result to the client.
            dispatchGroups();
        }
        else if(transaction.getResponse() instanceof Protein) {
            Protein protein = (Protein) transaction.getResponse();
            proteinCache.put(protein.getAccession(), protein);
            speciesCache.put(protein.getAccession(), protein.getTaxonID());

            for(Map<String, Boolean> batchRequest : proteinRequests) {
                if(batchRequest.containsKey((protein.getAccession()))) {
                    batchRequest.put(protein.getAccession(), true);
                }
            }

            for(PeptideMatch match : protein.getPeptides()) {
                if(peptideMatchCache.containsKey(new Pair<>(match.getSequence(), protein.getAccession()))) {
                    peptideMatchCache.get(new Pair<>(match.getSequence(), protein.getAccession())).add(match);
                } else {
                    List<PeptideMatch> matches = new ArrayList<>();
                    matches.add(match);
                    peptideMatchCache.put(new Pair<>(match.getSequence(), protein.getAccession()), matches);
                }
            }

            dispatchProteins();
        }
        else if(transaction.getResponse() instanceof PeptideList) {
            PeptideList pepListReceived = (PeptideList) transaction.getResponse();
            if(pepListReceived.getPeptideList() == null) {
                onErroneousResult(new GenericErroneousResult(transaction.getResponse(),
                        transaction.getRequestedName()));
                return;
            }
            peptiformListCache.put(transaction.getRequestedName(), pepListReceived);

            // Search for the peptide match requested peptide in the request cache
            // and update the pending requests
            for(Map<Triplet<String, Integer, String>, Boolean> batchRequest : peptiformRequests) {
                for(Map.Entry<Triplet<String, Integer, String>, Boolean> entry : batchRequest.entrySet()) {
                    if(transaction.getRequestedName().equals(entry.getKey().getA())) {
                        batchRequest.remove(entry.getKey());
                        batchRequest.put(new
                                Triplet<>(entry.getKey().getA(),
                                          entry.getKey().getB(),
                                          entry.getKey().getC()),
                                          true);
                        break;
                    }
                }
            }

            // update the peptiform cache
            for(Peptide peptiform : pepListReceived.getPeptideList()) {
                peptiformCache.put(peptiform.getId(), peptiform);
            }

            dispatchPeptiforms();
        }
        else if(transaction.getResponse() instanceof ErroneousResult) {
            onErroneousResult(((ErroneousResult) transaction.getResponse()));
        }
        else {
            onErroneousResult(new GenericErroneousResult(transaction.getResponse(),
                                                         transaction.getRequestedName()));
        }
    }

    @Override
    public void bind(DataClient client) {
        this.client = client;
    }

    @Override
    public boolean isGroupCached(String id) {
        return groupCache.containsKey(id);
    }

    @Override
    public boolean isProteinCached(String accession) {
        return proteinCache.containsKey(accession);
    }

    /*  The position and the protein ID are not needed to query the cache
        because the peptide matches contain aggregated, i.e. they are symbolic.
        So if two peptide matches have the same sequence they're only difference
        is the position, the rest of the data is exactly the same.
     */
    @Override
    public boolean isPeptideCached(String sequence, String proteinId, int position) {
        return isAnyPeptideCached(sequence);
    }

    @Override
    public boolean isAnyPeptideCached(String sequence) {
        return peptiformListCache.containsKey(sequence);
    }

    @Override
    public boolean isPeptiformCached(String varianceId) {
        return peptiformCache.containsKey(varianceId);
    }

    @Override
    public void requestGroups(List<String> ids) {
        Map<String, Boolean> request = new HashMap<>();

        groupRequests.add(request);

        for(String id : ids) {
            request.put(id, isGroupCached(id));
            if(!isGroupCached(id)) {
                // no need for explicit taxon annotation
                groupRetriever.retrieveData(id, null);
                // we could also check whether there's a pending request or not
                // in another batch
            }
        }
    }

    @Override
    public void requestProteins(List<String> accessions) {
        Map<String, Boolean> request = new HashMap<>();

        proteinRequests.add(request);

        for(String accession : accessions) {
            request.put(accession, isProteinCached(accession));
            if(!isProteinCached(accession)) {
                // no need for explicit taxon annotation
                proteinRetriever.retrieveData(accession, null);
                // we could also check whether there's a pending request or not
            }
        }
    }

    @Override
    public void requestPeptiforms(List<String> sequences, List<String> proteinIds, List<Integer> positions) {
        Map<Triplet<String, Integer, String>, Boolean> request = new HashMap<>();

        for(int i = 0; i < sequences.size() && i < proteinIds.size() && i < positions.size(); i++) {
            request.put(new Triplet<>(sequences.get(i), positions.get(i), proteinIds.get(i)),
                            isPeptideCached(sequences.get(i), proteinIds.get(i), positions.get(i)));
            if(!isPeptideCached(sequences.get(i), proteinIds.get(i), positions.get(i))) {
                //Hack that allows to filter by species the peptiform. The species wasn't in the original design
                // because it is implicit to the protein and symbolic peptides
                peptiformRetriever.retrieveData(sequences.get(i), speciesCache.get(proteinIds.get(i)));
                // we could also check whether there's a pending request or not
            }
            else {
                dispatchPeptiforms();
            }
        }

        peptiformRequests.add(request);
    }

    @Override
    public void requestPeptiforms(List<String> sequences, List<String> proteinIds) {
        List<Integer> positions = new ArrayList<>(sequences.size());
        while(positions.size() < sequences.size()) positions.add(-1);
        requestPeptiforms(sequences, proteinIds, positions);
    }

    @Override
    public List<Group> getCachedGroups(List<String> ids) {
        List<Group> groups = new ArrayList<>();
        for(String id : ids) {
            groups.add(groupCache.get(id));
        }
        return groups;
    }

    @Override
    public List<Protein> getCachedProteins(List<String> accessions) {
        List<Protein> proteins = new ArrayList<>();
        for(String accession : accessions) {
            proteins.add(proteinCache.get(accession));
        }
        return proteins;
    }

    @Override
    public List<PeptideWithPeptiforms> getCachedPeptiformLists(List<String> sequences, List<String> proteinIds, List<Integer> positions) {
        List<PeptideWithPeptiforms> peptideVarianceLists = new ArrayList<>();
        for(int i = 0; i < sequences.size() && i < proteinIds.size() && i < positions.size(); i++) {
            peptideVarianceLists.add(getCachedPeptiformList(sequences.get(i), proteinIds.get(i), positions.get(i)));
        }
        return peptideVarianceLists;
    }

    @Override
    public List<PeptideWithPeptiforms> getCachedPeptiformLists(List<String> sequences, List<String> proteinIds) {
        List<PeptideWithPeptiforms> peptideVarianceLists = new ArrayList<>();
        for(int i = 0; i < sequences.size() && i < proteinIds.size(); i++) {
            peptideVarianceLists.add(getCachedPeptiformList(sequences.get(i), proteinIds.get(i)));
        }
        return peptideVarianceLists;
    }

    @Override
    public List<Peptide> getCachedPeptiforms(List<String> varianceId) {
        List<Peptide> varianceList = new ArrayList<>();
        for (String id : varianceId) {
            varianceList.add(getCachedPeptiform(id));
        }
        return varianceList;
    }

    @Override
    public Group getCachedGroup(String id) {
        return groupCache.get(id);
    }

    @Override
    public Protein getCachedProtein(String accession) {
        return proteinCache.get(accession);
    }

    @Override
    public PeptideWithPeptiforms getCachedPeptiformList(String sequence, String proteinId, int position) {
        PeptideMatch match = null;

        for(PeptideMatch peptideMatch : peptideMatchCache.get(new Pair<>(sequence, proteinId))) {
            if(peptideMatch.getPosition() == position) {
                match = peptideMatch;
                break;
            }
        }
        return new PeptideWithPeptiforms(match, peptiformListCache.get(sequence));
    }

    @Override
    public PeptideWithPeptiforms getCachedPeptiformList(String sequence, String proteinId) {
        PeptideMatch match = peptideMatchCache.get(new Pair<>(sequence, proteinId)).isEmpty() ? null : peptideMatchCache.get(new Pair<>(sequence, proteinId)).get(0);
        return new PeptideWithPeptiforms(match, peptiformListCache.get(sequence));
    }

    @Override
    public Peptide getCachedPeptiform(String varianceId) {
        return peptiformCache.get(varianceId);
    }

    private void onErroneousResult(ErroneousResult error) {
        client.onRetrievalError(error);
    }

    private void dispatchGroups() {
        List<Map<String, Boolean>> toRemove = new ArrayList<>();
        List<Group> groups = new ArrayList<>();

        for(Map<String, Boolean> batchRequest : groupRequests) {
            if(!batchRequest.containsValue(false)) {
                toRemove.add(batchRequest);

                for(Map.Entry<String, Boolean> entry : batchRequest.entrySet()) {
                    groups.add(groupCache.get(entry.getKey()));
                }
            }
        }
        for(Map<String, Boolean> batchRequest : toRemove) {
            groupRequests.remove(batchRequest);
        }
        client.onGroupsRetrieved(groups);
    }

    private void dispatchProteins() {
        List<Map<String, Boolean>> toRemove = new ArrayList<>();
        List<Protein> proteins = new ArrayList<>();

        for(Map<String, Boolean> batchRequest : proteinRequests) {
            if(!batchRequest.containsValue(false)) {
                toRemove.add(batchRequest);

                for(Map.Entry<String, Boolean> entry : batchRequest.entrySet()) {
                    proteins.add(proteinCache.get(entry.getKey()));
                }
            }
        }
        for(Map<String, Boolean> batchRequest : toRemove) {
            proteinRequests.remove(batchRequest);
        }
        client.onProteinsRetrieved(proteins);
    }

    private void dispatchPeptiforms() {
        List<Map<Triplet<String, Integer, String>, Boolean>> toRemove = new ArrayList<>();
        List<PeptideWithPeptiforms> peptiforms = new ArrayList<>();
        for(Map<Triplet<String, Integer, String>, Boolean> batchRequest : peptiformRequests) {
            if(!batchRequest.containsValue(false)) {
                toRemove.add(batchRequest);

                for(Map.Entry<Triplet<String, Integer, String>, Boolean> entry : batchRequest.entrySet()) {
                    PeptideMatch match = null;
                    for(PeptideMatch peptideMatch : peptideMatchCache.get(new Pair<>(entry.getKey().getA(), entry.getKey().getC()))) {
                        if(peptideMatch.getPosition().equals(entry.getKey().getB()) ||
                           entry.getKey().getB() == -1) {
                            match = peptideMatch;
                            break;
                        }
                    }
                    peptiforms.add(new PeptideWithPeptiforms(match, peptiformListCache.get(entry.getKey().getA())));
                }
            }
        }
        for(Map<Triplet<String, Integer, String>, Boolean> batchRequest : toRemove) {
            peptiformRequests.remove(batchRequest);
        }
        client.onPeptiformListsRetrieved(peptiforms);
    }
}
