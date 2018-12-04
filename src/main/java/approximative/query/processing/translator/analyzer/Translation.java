package approximative.query.processing.translator.analyzer;

import approximative.query.processing.exceptions.PatternNotAllowedException;
import approximative.query.processing.graph.LabelUse;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.translator.analyzer.syntactic.analysis.*;

import java.util.List;
import java.util.Set;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/6/18.
 */
public class Translation {
    private Set<LabelUse> labelUseSet;

    public Translation(Set<LabelUse> labelUseSet) {
        this.labelUseSet = labelUseSet;
    }

    public List<Query> getTranslation(Query query) throws PatternNotAllowedException {

        Analyzer analyzer = null;
        if (new ConcatenationAnalyzer(query, labelUseSet).verify())
            analyzer =  new ConcatenationAnalyzer(query, labelUseSet);

        if (new SingleLabelsAnalyzer(query, labelUseSet).verify())
            analyzer = new SingleLabelsAnalyzer(query, labelUseSet);

        if (new SingleOptionalAnalyzer(query, labelUseSet).verify())
            analyzer = new SingleOptionalAnalyzer(query, labelUseSet);

        if (new KleenePlusAnalyzer(query, labelUseSet).verify())
            analyzer = new KleenePlusAnalyzer(query, labelUseSet);

        if (new KleeneStarAnalyzer(query, labelUseSet).verify())
            analyzer = new KleeneStarAnalyzer(query, labelUseSet);

        if (analyzer == null)
            throw new PatternNotAllowedException("The pattern on the query is not recognized.");

        return analyzer.translation();
    }







}
