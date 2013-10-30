package uk.ac.ebi.pride.proteomes.web.client.exceptions;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 25/10/13
 *         Time: 15:48
 */
public class InvalidJSONException extends Exception {
    public InvalidJSONException() {
    }
    public InvalidJSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
