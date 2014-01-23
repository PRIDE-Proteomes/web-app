package uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers;

import com.google.gwt.http.client.*;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.Transaction;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.TransactionHandler;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.UnacceptableResponse;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 08/01/14
 *         Time: 16:14
 */
class DataRequester implements RequestCallback {
    private final String id;
    private final Class responseType;
    private Collection<TransactionHandler> handlers = new ArrayList<>();

    public DataRequester(String identifier, String url, Class type, Collection<TransactionHandler> responseHandlers) {
        id = identifier;
        responseType = type;
        handlers = responseHandlers;

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("Accept", "application/json");

        try {
            builder.sendRequest(null, this);
        } catch(RequestException e) {
            onInvalidResponse(new UnacceptableResponse(0, "", e.getMessage(), responseType, id));
        }
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
        if(response == null) {
            onInvalidResponse(new UnacceptableResponse(0, "", "Could not contact the server.", responseType, id));
        } else if(response.getStatusText().equals("OK")) {
            onValidResponse(response);
        } else {
            onInvalidResponse(new UnacceptableResponse(response.getStatusCode(),
                                                       response.getStatusText(),
                                                       "The Server couldn't fulfill the request.",
                                                       responseType,
                                                       id));
        }
    }

    @Override
    public void onError(Request request, Throwable exception) {
        onInvalidResponse(new UnacceptableResponse(0, "", exception.getMessage(), responseType, id));
    }

    private void onValidResponse(Response response) {
        onTransactionDone(new Transaction(response, id, responseType));
    }

    private void onInvalidResponse(UnacceptableResponse r) {
        onTransactionDone(new Transaction(r, id, responseType));
    }

    private void onTransactionDone(Transaction transaction) {
        for(TransactionHandler handler : handlers) {
            handler.onTransactionFinished(transaction);
        }
    }
}
