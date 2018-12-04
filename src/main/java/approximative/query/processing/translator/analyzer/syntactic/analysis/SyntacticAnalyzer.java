package approximative.query.processing.translator.analyzer.syntactic.analysis;

import approximative.query.processing.graph.LabelUse;
import approximative.query.processing.translator.expression.PathPattern;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/6/18.
 */
abstract class SyntacticAnalyzer {

    Query inputQuery;
    List<PathPattern> pathPatterns;
    Set<LabelUse> labelUseSet;

    SyntacticAnalyzer(Query inputQuery, Set<LabelUse> labelUseSet) {
        this.inputQuery = inputQuery;
        this.pathPatterns = Util.getPathPatterns(inputQuery.getMatch());
        this.labelUseSet = labelUseSet;
    }

    static Set<String> getLabels(String stmt) {
        Set<String> labels = new HashSet<>();

        Pattern pattern = Pattern.compile("(:|[|])\\w+");
        Matcher matcher = pattern.matcher(stmt);

        while (matcher.find())
            labels.add(matcher.group(0).replaceAll("(:|[|])", ""));

        return labels;
    }

}
