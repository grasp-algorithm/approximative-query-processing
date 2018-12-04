package approximative.query.processing.translator.analyzer.syntactic.analysis;

import approximative.query.processing.translator.schema.Query;

import java.util.List;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/6/18.
 */
public interface Analyzer {

    String VAR_NODE_NAME = "x";
    String VAR_EDGE_NAME = "e";

    boolean verify();

    List<Query> translation();
}
