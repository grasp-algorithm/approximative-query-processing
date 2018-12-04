package approximative.query.processing.exceptions;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/26/18.
 */
public class NotPropertyFound extends Exception {

    public NotPropertyFound() {
    }

    public NotPropertyFound(String message) {
        super(message);
    }

    public NotPropertyFound(String message, Throwable cause) {
        super(message, cause);
    }

    public NotPropertyFound(Throwable cause) {
        super(cause);
    }

    public NotPropertyFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
