package approximative.query.processing.exceptions;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/28/18.
 */
public class InputLabelSetEmptyException extends RuntimeException {

    public InputLabelSetEmptyException() {
    }

    public InputLabelSetEmptyException(String message) {
        super(message);
    }

    public InputLabelSetEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InputLabelSetEmptyException(Throwable cause) {
        super(cause);
    }

    public InputLabelSetEmptyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
