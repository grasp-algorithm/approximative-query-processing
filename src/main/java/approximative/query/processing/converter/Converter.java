package approximative.query.processing.converter;

import approximative.query.processing.translator.schema.Query;

import java.io.IOException;
import java.util.Set;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/19/18.
 */
public interface Converter {

    /**
     *
     * @param workloadDirectory the workload directory
     * @param generateDisjunction the option to generate queries with disjunction
     * @return the list of queries in Pgql syntax
     */
    Set<Query> convert(String workloadDirectory, boolean generateDisjunction) throws IOException;
}
