package uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.Group;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.Transaction;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.TransactionHandler;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:10
 */
public class GroupRetriever implements TransactionHandler.DataRetriever {
    private final String root;

    private Collection<TransactionHandler> handlers = new
            ArrayList<TransactionHandler>();

    public GroupRetriever(String webServiceRoot) {
        this.root = webServiceRoot;
    }

    @Override
    public void retrieveData(String id) {
        String url = root + "/group/" + id;
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        builder.setHeader("Accept", "application/json");

        try {
            builder.sendRequest(null, this);
        } catch(RequestException e) {
            for(TransactionHandler handler : handlers) {
                handler.onDataRetrievalError(e);
            }
        }
    }

    @Override
    public void addHandler(TransactionHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
        Transaction trans;

        try {
            trans = new Transaction(response.getText(), Group.class);

            if(!response.getStatusText().equals("OK")) {
                throw new Exception(response.getStatusText());
            }

            for(TransactionHandler handler : handlers) {
                handler.onDataRetrieval(trans);
            }

        } catch (Exception e) {
            onError(request, e);
        }

    }

    @Override
    public void onError(Request request, Throwable exception) {
        for(TransactionHandler handler : handlers) {
            handler.onDataRetrievalError(exception);
        }
    }
}
