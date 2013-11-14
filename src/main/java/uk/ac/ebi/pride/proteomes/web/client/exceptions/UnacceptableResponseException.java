package uk.ac.ebi.pride.proteomes.web.client.exceptions;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 14/11/13
 *         Time: 10:33
 */
public class UnacceptableResponseException extends Exception {
    public UnacceptableResponseException() {
}
    public UnacceptableResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
