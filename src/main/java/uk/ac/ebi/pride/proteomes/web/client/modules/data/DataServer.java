package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import com.google.gwt.http.client.RequestCallback;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Group;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Protein;

/**
 * In this file are contained all the interfaces related to the data
 * server/provider module.
 *
 * There are two main interfaces, the retriever and the handler.
 * The retrievers are the ones that actually make the http request to the
 * restful service.
 *
 * The handlers are implemented  by whoever want to be notified
 * when a retriever gets the data. (usually the data provider)
 * Since we cannot use generics (java doesn't allow a class to implement the
 * same interface more than once, even though generics are being used),
 * A transaction object is used instead to encapsulate all kinds of data
 * retrieved. This makes the Transaction handler to check the type of the
 * data retrieved (using instanceof, for example)
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:05
 */
public interface DataServer {
    public interface DataClient {
        public void onGroupRetrieved(Group group);
        public void onProteinRetrieved(Protein protein);
        public void onPeptideRetrieved(Peptide peptide);

        public void onRetrievalError(String message);

        public void bindServer(DataServer server);
    }

    public interface TransactionHandler {
        public void onDataRetrievalError(Throwable exception);
        public void onDataRetrieval(Transaction transaction);
    }

    public interface DataRetriever extends RequestCallback {
        public void retrieveData(String query);
        public void addHandler(TransactionHandler handler);
    }

    public boolean isGroupCached(String id);
    public boolean isProteinCached(String accession);
    public boolean isPeptideCached(String sequence);

}
