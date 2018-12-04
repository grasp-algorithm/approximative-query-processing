package approximative.query.processing.exceptions;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/20/18.
 */
public class NoLabelFoundOnHNProperties extends Exception {

    public NoLabelFoundOnHNProperties() {
    }

    public NoLabelFoundOnHNProperties(String message) {
        super(message);
    }

    public NoLabelFoundOnHNProperties(String message, Throwable cause) {
        super(message, cause);
    }

    public NoLabelFoundOnHNProperties(Throwable cause) {
        super(cause);
    }

    public NoLabelFoundOnHNProperties(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
