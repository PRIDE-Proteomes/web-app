package uk.ac.ebi.pride.proteomes.web.client.modules.data;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 14/01/14
 *         Time: 11:39
 */
public class UnacceptableResponse implements DataServer.ErroneousResult {
    private final int code;
    private final String text;
    private final String description;
    private final Class type;
    private final String id;

    public UnacceptableResponse(int statusCode, String status, String statusCause,
                                Class requestedType, String requestedId) {
        code = statusCode;
        text = status;
        description = statusCause;
        type = requestedType;
        id = requestedId;
    }

    int getCode() {
        return code;
    }

    String getText() {
        return text;
    }

    @Override
    public Class getRequestedType() {
        return type;
    }

    @Override
    public String getRequestedIdentifier() {
        return id;
    }

    @Override
    public String getErrorDescription() {
        return description;
    }
}
