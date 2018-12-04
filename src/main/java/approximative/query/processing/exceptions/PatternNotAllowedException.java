package approximative.query.processing.exceptions;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/6/18.
 */
public class PatternNotAllowedException extends Exception {

    public PatternNotAllowedException() {
        super();
    }

    public PatternNotAllowedException(String message) {
        super(message);
    }

    public PatternNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PatternNotAllowedException(Throwable cause) {
        super(cause);
    }

    protected PatternNotAllowedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
