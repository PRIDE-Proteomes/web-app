package uk.ac.ebi.pride.proteomes.web.client.exceptions;

/**
 * @author Pau Ruiz Safont <psafont@ebi.ac.uk>
 *         Date: 04/11/13
 *         Time: 16:21
 */
public class IllegalModificationPositionException extends Exception {
    public IllegalModificationPositionException() {
    }
    public IllegalModificationPositionException(String message, Throwable cause) {
        super(message, cause);
    }
}
