package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.datamodel.factory.ModelFactory;
import uk.ac.ebi.pride.proteomes.web.client.exceptions.InvalidJSONException;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 29/10/13
 *         Time: 13:56
 */
public class Transaction {
    private final Object response;

    public Transaction(String json, Class type) throws InvalidJSONException {
        this.response = ModelFactory.getModelObject(type, json);
    }
    public Object getResponse() {
        return response;
    }

    public Class getType() {
        return response.getClass();
    }
}
