package approximative.query.processing.translator.expression;

import approximative.query.processing.translator.analyzer.Direction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/15/18.
 */
public class Predicate extends PathPattern {

    public Predicate(String expression, Direction direction) {
        super(expression, direction);
        path = false;
    }

    public boolean isDisjunction() {
        // [:knows|colleagues]
        return this.expression.contains("|");
    }


    public String getVariableName() {
        Pattern pattern = Pattern.compile("\\[\\w:");
        Matcher matcher = pattern.matcher(expression);

        if (matcher.find())
            return expression.substring(matcher.start(0), matcher.end(0));

        return "";
    }
}

