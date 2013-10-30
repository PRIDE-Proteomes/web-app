package uk.ac.ebi.pride.proteomes.web.client.exceptions;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 22/10/13
 *         Time: 10:54
 */
public class InconsistentStateException extends Exception {
    public InconsistentStateException() {
    }
    public InconsistentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
