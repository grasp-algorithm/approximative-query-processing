package approximative.query.processing.translator.analyzer.syntactic.analysis;

import approximative.query.processing.graph.LabelUse;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.util.Constants;
import approximative.query.processing.util.PrefixProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/6/18.
 */
public class SingleLabelsAnalyzer extends SyntacticAnalyzer implements Analyzer {

    public SingleLabelsAnalyzer(Query inputQuery, Set<LabelUse> labelUseSet) {
        super(inputQuery, labelUseSet);
    }

    @Override
    public boolean verify() {
        /* The simple label and disjunction are equals */
        return pathPatterns.size() == 1 && !pathPatterns.get(0).isPath();
    }

    @Override
    public List<Query> translation() {
        return getQueriesForSingleLabel();
    }


    List<Query> getQueriesForSingleLabel() {
        List<Query> queries = new ArrayList<>();

        Set<String> labelsInHNProperties = new HashSet<>(getLabels(inputQuery.getMatch()));
        labelsInHNProperties.retainAll(labelUseSet.stream().filter(LabelUse::isPresentOnHNProperties)
                .map(LabelUse::getLabel).collect(Collectors.toSet()));
        if (!labelsInHNProperties.isEmpty())
            queries.add(getNumberOfInnerEdges(labelsInHNProperties));


        Set<String> labelsOnCE = new HashSet<>(getLabels(inputQuery.getMatch()));
        labelsOnCE.retainAll(labelUseSet.stream().filter(LabelUse::isPresentOnCrossEdges)
                .map(LabelUse::getLabel).collect(Collectors.toSet()));
        if (!labelsOnCE.isEmpty())
            queries.addAll(getSumOfEdgeWeights(labelsOnCE));

        return queries;

    }


    private Query getNumberOfInnerEdges(Set<String> labels) {

        StringBuilder select = new StringBuilder("SELECT SUM(");
        String match = String.format("MATCH (%s)", VAR_NODE_NAME);
        StringBuilder where = new StringBuilder("WHERE ");

        int counter = 0;

        for (String label : labels) {
            select.append(String.format(" %1$s.%2$s * %1$s.%3$s ", VAR_NODE_NAME, PrefixProperty.PERCENTAGE.toString()
                    .concat(Constants.SEPARATOR).concat(label), PrefixProperty.NUMBER_OF_INNER_EDGES.toString()));

            where.append(String.format("%s.%s > 0", VAR_NODE_NAME, PrefixProperty.PERCENTAGE.toString()
                    .concat(Constants.SEPARATOR).concat(label)));

            if (++counter != labels.size()) {
                select.append("+");
                where.append(" OR ");
            }
        }
        select.append(")");


        return new Query(select.toString(), match, where.toString());
    }


    private List<Query> getSumOfEdgeWeights(Set<String> labels) {

        String select = String.format("SELECT SUM( e.%s )", PrefixProperty.EDGE_WEIGHT.toString());
        StringBuilder match = new StringBuilder("MATCH () -[e:");

        int counter = 0;
        for (String label : labels) {
            match.append(String.format("%s", label));

            if (++counter != labels.size())
                match.append("|");
        }

        match.append("]-> ()");

        return Collections.singletonList(new Query(select, match.toString(), inputQuery.getWhere()));
    }
}
