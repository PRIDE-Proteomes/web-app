package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Protein;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.GroupRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.PeptideRetriever;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers.ProteinRetriever;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:10
 */
public class DataProvider implements DataServer.TransactionHandler,
                                     DataServer {
    private final DataServer.DataClient client;

    private Map<String, Group> groupCache = new HashMap<String, Group>();
    private final GroupRetriever groupRetriever;

    private Map<String, Protein> proteinCache = new HashMap<String, Protein>();
    private final ProteinRetriever proteinRetriever;

    private Map<String, Peptide> peptideCache = new HashMap<String, Peptide>();
    private final PeptideRetriever peptideRetriever;


    public DataProvider(DataServer.DataClient client, String webRoot) {
        this.client = client;

        this.groupRetriever = new GroupRetriever(webRoot);
        this.proteinRetriever = new ProteinRetriever(webRoot);
        this.peptideRetriever = new PeptideRetriever(webRoot);
    }

    @Override
    public void onDataRetrievalError(Throwable exception) {
        client.onRetrievalError(exception.getMessage());
    }

    @Override
    public void onDataRetrieval(Transaction transaction) {
        if(transaction.getResponse() instanceof Group) {
            groupCache.put(((Group) transaction.getResponse()).getId(),
                            (Group) transaction.getResponse());
            client.onGroupRetrieved((Group) transaction.getResponse());
        }
        else if(transaction.getResponse() instanceof Protein) {
            proteinCache.put(((Protein) transaction.getResponse()).getAccession(),
                              (Protein) transaction.getResponse());
            client.onProteinRetrieved((Protein) transaction.getResponse());
        }
        else if(transaction.getResponse() instanceof Peptide) {
            peptideCache.put(((Peptide) transaction.getResponse()).getSequence(),
                              (Peptide) transaction.getResponse());
            client.onPeptideRetrieved((Peptide) transaction.getResponse());
        }
        else {
            onDataRetrievalError(new Exception("Internal Error, " +
                    "the developers need to update " + this.getClass().getName()));
        }
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
}
