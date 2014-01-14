package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import com.google.gwt.http.client.Response;
import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModelFactory;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InvalidJSONException;
import uk.ac.ebi.pride.proteomes.web.client.utils.StringUtils;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 29/10/13
 *         Time: 13:56
 */
public class Transaction {
    private final Object response;
    private final String requestedName;
    private final Class type;

    public Transaction(Response response, String name, Class type) {
        this.requestedName = name;
        this.type = type;
        Object resp = null;

        try {
            resp = ModelFactory.getModelObject(type, response.getText());
        } catch (InvalidJSONException e) {
            resp = new UnacceptableResponse(response.getStatusCode(), response.getStatusText(),
                    "The " + StringUtils.getShortName(getRequestedType())
                    + " with the identifier" + getRequestedName()
                    + " contained malformed data, please contact the Pride team",
                    type, name);
        }
        finally {
            this.response = resp;
        }
    }
    public Transaction(UnacceptableResponse response, String name, Class type) {
        this.requestedName = name;
        this.type = type;
        this.response = response;
    }

    public Object getResponse() {
        return response;
    }

    public String getRequestedName() {
        return requestedName;
    }

    public Class getRequestedType() {
        return type;
    }
}
