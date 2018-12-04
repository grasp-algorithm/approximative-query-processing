package approximative.query.processing.exceptions;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/9/18.
 */
public class NeedSumOfVerticesException extends RuntimeException {
    public NeedSumOfVerticesException() {
    }

    public NeedSumOfVerticesException(String message) {
        super(message);
    }

    public NeedSumOfVerticesException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeedSumOfVerticesException(Throwable cause) {
        super(cause);
    }

    public NeedSumOfVerticesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
