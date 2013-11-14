package uk.ac.ebi.pride.proteomes.web.client.modules.data.retrievers;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.Peptide;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InvalidJSONException;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.UnacceptableResponseException;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.Transaction;
import uk.ac.ebi.pride.proteomes.web.client.modules.data.TransactionHandler;

import java.util.ArrayList;
import java.util.Collection;
/**
 * This class retrieves the variances of a specified peptide sequence
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 29/10/13
 *         Time: 16:17
 */

public class PeptideVarianceRetriever implements TransactionHandler.DataRetriever {
    private final String root;

    private Collection<TransactionHandler> handlers = new
            ArrayList<TransactionHandler>();

    public PeptideVarianceRetriever(String webServiceRoot) {
        this.root = webServiceRoot;
    }

    @Override
    public void retrieveData(String id) {
        String url = root + "/peptide/" + id;
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

        if(response == null) {
            onError(request, new Exception("Error: Could not contact the " +
                    "server."));
            return;
        }
        try {
            if(!response.getStatusText().equals("OK")) {
                throw new UnacceptableResponseException();
            }

            trans = new Transaction(response.getText(), Peptide.class);

            for(TransactionHandler handler : handlers) {
                handler.onDataRetrieval(trans);
            }

        } catch(InvalidJSONException e) {
            onError(request, e);
        } catch(UnacceptableResponseException e) {
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
