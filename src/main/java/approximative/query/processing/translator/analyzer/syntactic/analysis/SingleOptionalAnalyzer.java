package approximative.query.processing.translator.analyzer.syntactic.analysis;

import approximative.query.processing.exceptions.NeedSumOfVerticesException;
import approximative.query.processing.graph.LabelUse;
import approximative.query.processing.translator.schema.Query;

import java.util.List;
import java.util.Set;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/6/18.
 */
public class SingleOptionalAnalyzer extends SingleLabelsAnalyzer implements Analyzer {

    public SingleOptionalAnalyzer(Query inputQuery, Set<LabelUse> labelUseSet) {
        super(inputQuery, labelUseSet);
    }

    @Override
    public boolean verify() {
        return pathPatterns.size() == 1 && pathPatterns.get(0).isPath() &&
                pathPatterns.get(0).toPathExpression().isOptional();
    }

    @Override
    public List<Query> translation() {
        List<Query> queries = getQueriesForSingleLabel();
        if (queries.isEmpty())
            throw new NeedSumOfVerticesException("Need the sum of the vertices");

        queries.get(0).setSumVertices(true);
        return queries;
    }
}
