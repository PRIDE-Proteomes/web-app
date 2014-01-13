package uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers;

import com.google.gwt.http.client.*;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InvalidJSONException;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.UnacceptableResponseException;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.Transaction;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.TransactionHandler;
import uk.ac.ebi.pride.proteomes.web.client.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 08/01/14
 *         Time: 16:14
 */
public class DataRequester implements RequestCallback {
    final String id;
    final Class responseType;
    private Collection<TransactionHandler> handlers = new ArrayList<TransactionHandler>();

    public DataRequester(String identifier, String url, Class type, Collection<TransactionHandler> responseHandlers) {
        id = identifier;
        responseType = type;
        handlers = responseHandlers;

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("Accept", "application/json");

        try {
            builder.sendRequest(null, this);
        } catch(RequestException e) {
            onDataRetrievalError(e);
        }
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
        if(response == null) {
            onDataRetrievalError(new Exception("Error: Could not contact the server."));
        } else if(response.getStatusText().equals("OK")) {
            processResponse(response.getText());
        } else {
            onDataRetrievalError(new UnacceptableResponseException());
        }
    }

    @Override
    public void onError(Request request, Throwable exception) {
        onDataRetrievalError(exception);
    }

    private void processResponse(String response) {
        Transaction trans;
        try {
            trans = new Transaction(response, responseType);
            onDataRetrieval(trans);
        } catch(InvalidJSONException e) {
            if(response.equals("")) {

                onDataRetrievalError(new InvalidJSONException("The requested "
                        + StringUtils.getShortName(responseType)
                        + " isn't in the database", e));
            } else {
                onDataRetrievalError(e);
            }
        }
    }

    private void onDataRetrievalError(Throwable e) {
        String cause = StringUtils.getShortName(responseType) + " " + id;
        for(TransactionHandler handler : handlers) {
            handler.onDataRetrievalError(e, cause);
        }
    }

    private void onDataRetrieval(Transaction transaction) {
        for(TransactionHandler handler : handlers) {
            handler.onDataRetrieval(transaction);
        }
    }
}
