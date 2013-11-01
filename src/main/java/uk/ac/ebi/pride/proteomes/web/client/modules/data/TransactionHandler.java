package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import com.google.gwt.http.client.RequestCallback;

/**
 * There are two interfaces, the retriever and the handler.
 * The retriever is the ones that actually make the http request to the
 * restful service and uses the callbacks to deliver the information
 * asynchronously.
 *
 * The handler is implemented by whoever want to be notified when a retriever
 * gets the data. (usually the data provider)
 * Since we cannot use generics (java doesn't allow a class to implement the
 * same interface more than once, even though generics are being used),
 * a transaction object is used instead to encapsulate all kinds of data
 * retrieved. This means the transaction handler or whoever needs to unpack
 * the data inside the transaction has the responsibility to
 * check the type of the data retrieved (using instanceof, for example)
 *
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 */
public interface TransactionHandler {
    public interface DataRetriever extends RequestCallback {
        public void retrieveData(String query);
        public void addHandler(TransactionHandler handler);
    }

    void onDataRetrievalError(Throwable exception);
    void onDataRetrieval(Transaction transaction);
}
