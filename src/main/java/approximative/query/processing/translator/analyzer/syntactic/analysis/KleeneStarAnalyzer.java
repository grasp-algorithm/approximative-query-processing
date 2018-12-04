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
public class KleeneStarAnalyzer extends KleenePlusAnalyzer implements Analyzer {


    public KleeneStarAnalyzer(Query inputQuery, Set<LabelUse> labelUseSet) {
        super(inputQuery, labelUseSet);
    }

    @Override
    public boolean verify() {
        return pathPatterns.size() == 1 && pathPatterns.get(0).isPath() && pathPatterns.get(0).toPathExpression().hasKleeneStar();
    }

    @Override
    public List<Query> translation() {
        List<Query> queries = getQueriesForKleenePlus();
        if (queries.isEmpty())
            throw new NeedSumOfVerticesException("Need the sum of the vertices because none query founded.");

        queries.get(0).setSumVertices(true);
        return queries;
    }
}
