package approximative.query.processing.translator.analyzer.syntactic.analysis;

import approximative.query.processing.graph.LabelUse;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.util.Constants;
import approximative.query.processing.util.PrefixProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/6/18.
 */
public class KleenePlusAnalyzer extends SyntacticAnalyzer implements Analyzer {

    public KleenePlusAnalyzer(Query inputQuery, Set<LabelUse> labelUseSet) {
        super(inputQuery, labelUseSet);
    }

    @Override
    public boolean verify() {
        return pathPatterns.size() == 1 && pathPatterns.get(0).isPath() && pathPatterns.get(0).toPathExpression()
                .hasKleenePlus();
    }

    @Override
    public List<Query> translation() {
        return getQueriesForKleenePlus();
    }


    List<Query> getQueriesForKleenePlus() {
        List<Query> queries = new ArrayList<>();
        Set<String> labels = getLabels(inputQuery.getMatch());
        labels.removeIf(l -> labelUseSet.stream().noneMatch(lu -> lu.getLabel().equals(l) &&
                lu.isPresentOnReachCount()));
        if (!labels.isEmpty())
            queries.add(getReachabilityCountOnHN(labels));

        labels = getLabels(inputQuery.getMatch());
        labels.removeIf(l -> labelUseSet.stream().noneMatch(lu -> lu.getLabel().equals(l) &&
                lu.isPresentOnCrossEdges()));

        if (!labels.isEmpty())
            queries.add(getReachabilityOnCrossEdges(labels));

        return queries;
    }

    private Query getReachabilityCountOnHN(Set<String> labels) {
        StringBuilder select = new StringBuilder("SELECT SUM(");
        StringBuilder where = new StringBuilder("WHERE ");

        int counter = 0;
        for (String label : labels) {

            String reachCountByLabel = PrefixProperty.REACHABILITY_COUNT.toString().concat(Constants.SEPARATOR)
                    .concat(label);
            String pathOutByLabel = PrefixProperty.PATH_OUT.toString().concat(Constants.SEPARATOR)
                    .concat(label);
            String pathInByLabel = PrefixProperty.REACH_PATH_IN_BY_LABEL.toString().concat(Constants.SEPARATOR)
                    .concat(label);

            select.append(String.format("%s.%s", VAR_NODE_NAME, reachCountByLabel));

            if (labelUseSet.stream().anyMatch(l -> l.getLabel().equals(label) && l.isPresentOnPathOutCount()))
                select.append(String.format("+ %s.%s", VAR_NODE_NAME, pathOutByLabel));

            if (labelUseSet.stream().anyMatch(l -> l.getLabel().equals(label) && l.isPresentOnPathInCount()))
                select.append(String.format("+ %s.%s", VAR_NODE_NAME, pathInByLabel));

            where.append(String.format("%1$s.%2$s > 0", VAR_NODE_NAME, reachCountByLabel));

            if (++counter != labels.size()) {
                where.append(" OR ");
                select.append(" + ");
            }
        }

        select.append(") ");

        return new Query(select.toString(), String.format("MATCH (%s)", VAR_NODE_NAME), where.toString())
                .setSumVertices(false);
    }


    private Query getReachabilityOnCrossEdges(Set<String> labels) {

        StringBuilder match = new StringBuilder("MATCH ");
        for (String label : labels)
            match.append(String.format("() -[e:%s]-> ()", label));

        return new Query(String.format("SELECT SUM(e.%s) ", PrefixProperty.EDGE_WEIGHT.toString()),
                match.toString(),
                inputQuery.getWhere());
    }
}
