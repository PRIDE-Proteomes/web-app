package uk.ac.ebi.pride.proteomes.web.client.modules.data;

import uk.ac.ebi.pride.proteomes.web.client.utils.StringUtils;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 14/01/14
 *         Time: 13:58
 */
public class GenericErroneousResult implements DataServer.ErroneousResult {
    private final Object object;
    private final String name;

    public GenericErroneousResult(Object result, String requestedName) {
        object = result;
        name = requestedName;
    }

    @Override
    public Class getRequestedType() {
        return object.getClass();
    }

    @Override
    public String getRequestedIdentifier() {
        return name;
    }

    @Override
    public String getErrorDescription() {
        return "Received the unknown data type \""
               + StringUtils.getShortName(getRequestedType())
               + "\" from the server with th id \""
               + getRequestedIdentifier()
               + "\".";
    }
}
