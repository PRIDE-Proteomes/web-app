package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Protein;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.GroupRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.PeptideRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.ProteinRetriever;

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
    private List<Map<String, Boolean>> groupRequests;
    private final GroupRetriever groupRetriever;

    private Map<String, Protein> proteinCache = new HashMap<String, Protein>();
    private List<Map<String, Boolean>> proteinRequests;
    private final ProteinRetriever proteinRetriever;

    private Map<String, Peptide> peptideCache = new HashMap<String, Peptide>();
    private List<Map<String, Boolean>> peptideRequests;
    private final PeptideRetriever peptideRetriever;

    public DataProvider(String webRoot) {
        groupRequests = new ArrayList<Map<String, Boolean>>();
        groupRetriever = new GroupRetriever(webRoot);
        groupRetriever.addHandler(this);

        proteinRequests = new ArrayList<Map<String, Boolean>>();
        proteinRetriever = new ProteinRetriever(webRoot);
        proteinRetriever.addHandler(this);

        peptideRequests = new ArrayList<Map<String, Boolean>>();
        peptideRetriever = new PeptideRetriever(webRoot);
        peptideRetriever.addHandler(this);
    }

    @Override
    public void onDataRetrievalError(Throwable exception) {
        client.onRetrievalError(exception.getMessage());
    }

    @Override
    public void onDataRetrieval(Transaction transaction) {
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

            dispatchProteins();
        }
        else if(transaction.getResponse() instanceof Peptide) {
            Peptide peptide = (Peptide) transaction.getResponse();
            peptideCache.put(peptide.getSequence(), peptide);

            for(Map<String, Boolean> batchRequest : peptideRequests) {
                if(batchRequest.containsKey((peptide.getSequence()))) {
                    batchRequest.put(peptide.getSequence(), true);
                }
            }

            dispatchPeptides();
        }
        else {
            onDataRetrievalError(new Exception("Internal Error, " +
                    "the developers need to update " + this.getClass().getName()));
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

    @Override
    public boolean isPeptideCached(String sequence) {
        return peptideCache.containsKey(sequence);
    }

    @Override
    public void requestGroups(String[] ids) {
        Map<String, Boolean> request = new HashMap<String, Boolean>();

        groupRequests.add(request);

        for(String id : ids) {
            request.put(id, isGroupCached(id));
            if(isGroupCached(id)) {
                groupRetriever.retrieveData(id);
                // we could also check whether there's a pending request or not
                // in another batch
            }
        }
    }

    @Override
    public void requestProteins(String[] accessions) {
        Map<String, Boolean> request = new HashMap<String, Boolean>();

        proteinRequests.add(request);

        for(String accession : accessions) {
            request.put(accession, isProteinCached(accession));
            if(isProteinCached(accession)) {
                proteinRetriever.retrieveData(accession);
                // we could also check whether there's a pending request or not
            }
        }
    }

    @Override
    public void requestPeptides(String[] sequences) {
        Map<String, Boolean> request = new HashMap<String, Boolean>();

        peptideRequests.add(request);

        for(String sequence : sequences) {
            request.put(sequence, isPeptideCached(sequence));
            if(isPeptideCached(sequence)) {
                peptideRetriever.retrieveData(sequence);
                // we could also check whether there's a pending request or not
            }
        }
    }

    private void dispatchGroups() {
        for(Map<String, Boolean> batchRequest : groupRequests) {
            if(!batchRequest.containsValue(false)) {
                groupRequests.remove(batchRequest);

                List<Group> groups = new ArrayList<Group>();
                for(Map.Entry<String, Boolean> entry : batchRequest.entrySet()) {
                    groups.add(getGroup(entry.getKey()));
                }
                client.onGroupsRetrieved(groups);
            }
        }
    }

    private void dispatchProteins() {
        for(Map<String, Boolean> batchRequest : proteinRequests) {
            if(!batchRequest.containsValue(false)) {
                proteinRequests.remove(batchRequest);

                List<Protein> proteins = new ArrayList<Protein>();
                for(Map.Entry<String, Boolean> entry : batchRequest.entrySet()) {
                    proteins.add(getProtein(entry.getKey()));
                }
                client.onProteinsRetrieved(proteins);
            }
        }
    }

    private void dispatchPeptides() {
        for(Map<String, Boolean> batchRequest : peptideRequests) {
            if(!batchRequest.containsValue(false)) {
                peptideRequests.remove(batchRequest);

                List<Peptide> peptides = new ArrayList<Peptide>();
                for(Map.Entry<String, Boolean> entry : batchRequest.entrySet()) {
                    peptides.add(getPeptide(entry.getKey()));
                }
                client.onPeptidesRetrieved(peptides);
            }
        }
    }

    private Group getGroup(String id) {
        return groupCache.get(id);
    }

    private Protein getProtein(String accession) {
        return proteinCache.get(accession);
    }

    private Peptide getPeptide(String sequence) {
        return peptideCache.get(sequence);
    }
}
