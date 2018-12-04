package approximative.query.processing.util;

import approximative.query.processing.translator.expression.PathExpression;
import approximative.query.processing.translator.expression.PathPattern;
import approximative.query.processing.translator.expression.Predicate;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.translator.analyzer.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/19/18.
 */
public class Util {

    private Util() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static List<String> getLabels(String matchClause) {
        List<String> labels = new ArrayList<>();

        Pattern pattern = Pattern.compile("(:|[|])\\w+");
        Matcher matcher = pattern.matcher(matchClause);

        while (matcher.find())
            labels.add(matcher.group(0).replaceAll("(:|[|])", ""));


        return labels;
    }

    public static String getType(Query query) {
        List<PathPattern> pathPatterns = getPathPatterns(query.getMatch());
        if (pathPatterns.size() > 1)
            return "CONCATENATION";
        else if (pathPatterns.get(0).isPath())
            if (pathPatterns.get(0).toPathExpression().hasKleenePlus())
                return "KLEENE PLUS";
            else if (pathPatterns.get(0).toPathExpression().hasKleeneStar())
                return "KLEENE STAR";
            else
                return "OPTIONAL";
        else if (pathPatterns.get(0).toPredicate().isDisjunction())
            return "DISJUNCTION";
        else
            return "SINGLE LABEL";
    }

    public static int getLength(Query query) {
        List<PathPattern> pathPatterns = getPathPatterns(query.getMatch());
        if (getType(query).equals("DISJUNCTION"))
            return getLabels(query.getMatch()).size();

        return pathPatterns.size();
    }

    public static List<PathPattern> getPathPatterns(String stmt) {
        List<PathPattern> predicates = new ArrayList<>();

        String expressionRegexp = "-/:\\w+[+|*|?|{\\d*(,\\d+)?}]+/-";
        String predicateRegexp = "-\\[(\\w)?:(\\w+\\|)*\\w+]-";

        String regexp = String.format("(%s)|(%s)", expressionRegexp, predicateRegexp);

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(stmt);

        Pattern rightDirection;
        Pattern leftDirection;

        while (matcher.find()) {

            int start = matcher.start(0);
            int end = matcher.end(0);

            boolean predicate = false;

            if (Pattern.compile(predicateRegexp).matcher(stmt.substring(start, end)).matches()) {
                rightDirection = Pattern.compile(predicateRegexp.concat(Direction.RIGHT.getOrientation()));
                leftDirection = Pattern.compile(Direction.LEFT.getOrientation().concat(predicateRegexp));

                predicate = true;

            } else {
                rightDirection = Pattern.compile(expressionRegexp.concat(Direction.RIGHT.getOrientation()));
                leftDirection = Pattern.compile(Direction.LEFT.getOrientation().concat(expressionRegexp));
            }

            Matcher directionMatcher = leftDirection.matcher(stmt.substring(start - 2, end));
            Direction direction = null;

            if (directionMatcher.find()) direction = Direction.LEFT;
            else {
                directionMatcher = rightDirection.matcher(stmt.substring(start, end + 2));
                if (directionMatcher.find()) direction = Direction.RIGHT;

            }

            if (predicate)
                predicates.add(new Predicate(directionMatcher.group(0), direction));
            else
                predicates.add(new PathExpression(directionMatcher.group(0), direction));
        }

        return predicates;
    }
}
