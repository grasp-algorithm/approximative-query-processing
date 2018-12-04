package approximative.query.processing.translator.analyzer;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/15/18.
 */
public enum Direction {
    RIGHT(">"),
    LEFT("<");

    private String orientation;

    Direction(String orientation) {
        this.orientation = orientation;
    }

    public String getOrientation() {
        return orientation;
    }
}
