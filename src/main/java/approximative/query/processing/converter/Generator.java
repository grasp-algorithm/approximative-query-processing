package approximative.query.processing.converter;

import approximative.query.processing.translator.schema.Query;
import oracle.pgx.api.PgxGraph;

import java.util.Set;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/24/18.
 */
public interface Generator {


    /**
     *
     * @param graph
     * @return
     */
    Set<Query> generate(PgxGraph graph);
}
