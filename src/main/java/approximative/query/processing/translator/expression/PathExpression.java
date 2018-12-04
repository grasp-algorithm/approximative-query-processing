package approximative.query.processing.translator.expression;

import approximative.query.processing.translator.analyzer.Direction;
import groovy.lang.Tuple2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/15/18.
 */
public class PathExpression extends PathPattern {

    public PathExpression(String expression, Direction direction) {
        super(expression, direction);
        this.path = true;
    }

    public boolean isOptional() {
        // /:knows?/
        return this.expression.contains("?");
    }

    public boolean hasKleeneStar() {
        return this.expression.contains("*");
    }

    public boolean hasKleenePlus() {
        return this.expression.contains("+");
    }

    public boolean hasRepetitionQuantifierBounded() {
        return this.expression.contains("{");
    }

    //fixme
    public Tuple2<Integer, Integer> getRepetitionQuantifierBounderies() {
        Pattern pattern = Pattern.compile("\\{\\d*,?\\d*}");
        Matcher matcher = pattern.matcher(expression);

        String quantifier = matcher.group(0);
        String[] boundaries = quantifier.split(",");
        // {n} exactly n
        if (boundaries.length == 1)
            return new Tuple2<>(Integer.parseInt(boundaries[0]), Integer.parseInt(boundaries[0]));


        return new Tuple2<>(1, 20);

    }
}
