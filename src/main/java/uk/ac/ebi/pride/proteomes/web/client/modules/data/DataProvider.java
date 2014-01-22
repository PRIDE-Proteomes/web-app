package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.PeptideWithVariances;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.*;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.GroupRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.PeptideVarianceRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.ProteinRetriever;
import uk.ac.ebi.pride.proteomes.web.client.utils.Pair;

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

    private Map<String, Group> groupCache = new HashMap<String, Group>();
    private List<Map<String, Boolean>> groupRequests = new ArrayList<Map<String, Boolean>>();
    private final GroupRetriever groupRetriever;

    private Map<String, Protein> proteinCache = new HashMap<String, Protein>();
    private List<Map<String, Boolean>> proteinRequests = new ArrayList<Map<String, Boolean>>();
    private final ProteinRetriever proteinRetriever;

    // We cache the PeptideLists retrieved as well as store the mapping between
    // sequence and peptide matches, this way we can build PeptideWithVariances
    // dynamically while avoid being a time hog.
    private Map<String, PeptideList> peptideVarianceListCache = new HashMap<String, PeptideList>();
    private Map<String, List<PeptideMatch>> peptideMatchCache = new HashMap<String, List<PeptideMatch>>();
    private List<Map<Pair<String, Integer>, Boolean>> peptideVarianceRequests = new ArrayList<Map<Pair<String, Integer>, Boolean>>();
    private final PeptideVarianceRetriever peptideVarianceRetriever;

    private Map<String, Peptide> peptideVarianceCache = new HashMap<String, Peptide>();

    public DataProvider(String webServiceRoot) {
        groupRetriever = new GroupRetriever(webServiceRoot);
        groupRetriever.addHandler(this);

        proteinRetriever = new ProteinRetriever(webServiceRoot);
        proteinRetriever.addHandler(this);

        peptideVarianceRetriever = new PeptideVarianceRetriever(webServiceRoot);
        peptideVarianceRetriever.addHandler(this);
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

            for(Map<String, Boolean> batchRequest : proteinRequests) {
                if(batchRequest.containsKey((protein.getAccession()))) {
                    batchRequest.put(protein.getAccession(), true);
                }
            }

            for(PeptideMatch match : protein.getPeptides()) {
                if(peptideMatchCache.containsKey(match.getSequence())) {
                    peptideMatchCache.get(match.getSequence()).add(match);
                } else {
                    List<PeptideMatch> matches = new ArrayList<PeptideMatch>();
                    matches.add(match);
                    peptideMatchCache.put(match.getSequence(), matches);
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
            peptideVarianceListCache.put(transaction.getRequestedName(), pepListReceived);

            // Search for the peptide match requested peptide in the request cache
            // and update the pending requests
            for(Map<Pair<String, Integer>, Boolean> batchRequest : peptideVarianceRequests) {
                for(Map.Entry<Pair<String, Integer>, Boolean> entry : batchRequest.entrySet()) {
                    if(transaction.getRequestedName().equals(entry.getKey().getA())) {
                        batchRequest.remove(entry.getKey());
                        batchRequest.put(new
                                Pair<String, Integer>(entry.getKey().getA(),
                                                      entry.getKey().getB()),
                                                      true);
                        break;
                    }
                }
            }

            // update the variance cache
            for(Peptide variance : pepListReceived.getPeptideList()) {
                peptideVarianceCache.put(variance.getId(), variance);
            }

            dispatchPeptideVariances();
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
        return peptideVarianceListCache.containsKey(sequence);
    }

    @Override
    public boolean isPeptideVarianceCached(String varianceId) {
        return peptideVarianceCache.containsKey(varianceId);
    }

    @Override
    public void requestGroups(List<String> ids) {
        Map<String, Boolean> request = new HashMap<String, Boolean>();

        groupRequests.add(request);

        for(String id : ids) {
            request.put(id, isGroupCached(id));
            if(!isGroupCached(id)) {
                groupRetriever.retrieveData(id);
                // we could also check whether there's a pending request or not
                // in another batch
            }
        }
    }

    @Override
    public void requestProteins(List<String> accessions) {
        Map<String, Boolean> request = new HashMap<String, Boolean>();

        proteinRequests.add(request);

        for(String accession : accessions) {
            request.put(accession, isProteinCached(accession));
            if(!isProteinCached(accession)) {
                proteinRetriever.retrieveData(accession);
                // we could also check whether there's a pending request or not
            }
        }
    }

    @Override
    public void requestPeptideVariances(List<String> sequences, List<String> proteinIds, List<Integer> positions) {
        Map<Pair<String, Integer>, Boolean> request = new HashMap<Pair<String, Integer>, Boolean>();

        peptideVarianceRequests.add(request);

        for(int i = 0; i < sequences.size() && i < proteinIds.size() && i < positions.size(); i++) {
            request.put(new Pair<String, Integer>(sequences.get(i), positions.get(i)),
                            isPeptideCached(sequences.get(i), proteinIds.get(i), positions.get(i)));
            if(!isPeptideCached(sequences.get(i), proteinIds.get(i), positions.get(i))) {
                peptideVarianceRetriever.retrieveData(sequences.get(i));
                // we could also check whether there's a pending request or not
            }
            else {
                dispatchPeptideVariances();
            }
        }
    }

    @Override
    public void requestPeptideVariances(List<String> sequences, List<String> proteinIds) {
        List<Integer> positions = new ArrayList<Integer>(sequences.size());
        while(positions.size() < sequences.size()) positions.add(-1);
        requestPeptideVariances(sequences, proteinIds, positions);
    }

    @Override
    public List<Group> getCachedGroups(List<String> ids) {
        List<Group> groups = new ArrayList<Group>();
        for(String id : ids) {
            groups.add(groupCache.get(id));
        }
        return groups;
    }

    @Override
    public List<Protein> getCachedProteins(List<String> accessions) {
        List<Protein> proteins = new ArrayList<Protein>();
        for(String accession : accessions) {
            proteins.add(proteinCache.get(accession));
        }
        return proteins;
    }

    @Override
    public List<PeptideWithVariances> getCachedPeptideVarianceLists(List<String> sequences, List<String> proteinIds, List<Integer> positions) {
        List<PeptideWithVariances> peptideVarianceLists = new ArrayList<PeptideWithVariances>();
        for(int i = 0; i < sequences.size() && i < proteinIds.size() && i < positions.size(); i++) {
            peptideVarianceLists.add(getCachedPeptideVarianceList(sequences.get(i), proteinIds.get(i), positions.get(i)));
        }
        return peptideVarianceLists;
    }

    @Override
    public List<PeptideWithVariances> getCachedPeptideVarianceLists(List<String> sequences, List<String> proteinIds) {
        List<PeptideWithVariances> peptideVarianceLists = new ArrayList<PeptideWithVariances>();
        for(String sequence : sequences) {
            //todo check bounds
            peptideVarianceLists.add(getCachedPeptideVarianceList(sequence, proteinIds.get(0)));
        }
        return peptideVarianceLists;
    }

    @Override
    public List<Peptide> getCachedPeptideVariances(List<String> varianceId) {
        List<Peptide> varianceList = new ArrayList<Peptide>();
        for (String id : varianceId) {
            varianceList.add(getCachedPeptideVariance(id));
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
    public PeptideWithVariances getCachedPeptideVarianceList(String sequence, String proteinId, int position) {
        PeptideMatch match = null;

        for(PeptideMatch peptideMatch : peptideMatchCache.get(sequence)) {
            if(peptideMatch.getPosition() == position) {
                match = peptideMatch;
                break;
            }
        }
        return new PeptideWithVariances(match, peptideVarianceListCache.get(sequence));
    }

    @Override
    public PeptideWithVariances getCachedPeptideVarianceList(String sequence, String proteinId) {
        PeptideMatch match = peptideMatchCache.get(sequence).isEmpty() ? null : peptideMatchCache.get(sequence).get(0);
        return new PeptideWithVariances(match, peptideVarianceListCache.get(sequence));
    }

    @Override
    public Peptide getCachedPeptideVariance(String varianceId) {
        return peptideVarianceCache.get(varianceId);
    }

    private void onErroneousResult(ErroneousResult error) {
        client.onRetrievalError(error);
    }

    private void dispatchGroups() {
        List<Map<String, Boolean>> toRemove = new ArrayList<Map<String, Boolean>>();
        List<Group> groups = new ArrayList<Group>();

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
        List<Map<String, Boolean>> toRemove = new ArrayList<Map<String, Boolean>>();
        List<Protein> proteins = new ArrayList<Protein>();

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

    private void dispatchPeptideVariances() {
        List<Map<Pair<String, Integer>, Boolean>> toRemove = new ArrayList<Map<Pair<String, Integer>, Boolean>>();
        List<PeptideWithVariances> peptideVariances = new ArrayList<PeptideWithVariances>();
        for(Map<Pair<String, Integer>, Boolean> batchRequest : peptideVarianceRequests) {
            if(!batchRequest.containsValue(false)) {
                toRemove.add(batchRequest);

                for(Map.Entry<Pair<String, Integer>, Boolean> entry : batchRequest.entrySet()) {
                    PeptideMatch match = null;
                    for(PeptideMatch peptideMatch : peptideMatchCache.get(entry.getKey().getA())) {
                        if(peptideMatch.getPosition().equals(entry.getKey().getB()) ||
                           entry.getKey().getB() == -1) {
                            match = peptideMatch;
                            break;
                        }
                    }
                    peptideVariances.add(new PeptideWithVariances(match, peptideVarianceListCache.get(entry.getKey().getA())));
                }
            }
        }
        for(Map<Pair<String, Integer>, Boolean> batchRequest : toRemove) {
            peptideVarianceRequests.remove(batchRequest);
        }
        client.onPeptideVarianceListsRetrieved(peptideVariances);
    }
}
