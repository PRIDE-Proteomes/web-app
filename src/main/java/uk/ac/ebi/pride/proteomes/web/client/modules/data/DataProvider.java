package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import com.google.gwt.dev.util.Pair;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Protein;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.GroupRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.PeptideRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.ProteinRetriever;

import java.util.*;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:10
 */
public class DataProvider implements DataServer.TransactionHandler,
                                     DataServer {
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

        proteinRetriever = new ProteinRetriever(webRoot);
        proteinRetriever.addHandler(this);

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

            // If there-s any request that-s been processed completely we
            // should return the result to the client.
            dispatchGroups();
        }
        else if(transaction.getResponse() instanceof Protein) {
            proteinCache.put(((Protein) transaction.getResponse()).getAccession(),
                              (Protein) transaction.getResponse());
            //todo
            // client.onProteinsRetrieved((Protein) transaction.getResponse());
        }
        else if(transaction.getResponse() instanceof Peptide) {
            peptideCache.put(((Peptide) transaction.getResponse()).getSequence(),
                              (Peptide) transaction.getResponse());
            // todo
            // client.onPeptidesRetrieved((Peptide) transaction.getResponse());
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

    @Override
    public Group getGroup(String id) {
        return groupCache.get(id);
    }

    @Override
    public Protein getProtein(String accession) {
        return proteinCache.get(accession);
    }

    @Override
    public Peptide getPeptide(String sequence) {
        return peptideCache.get(sequence);
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
}
