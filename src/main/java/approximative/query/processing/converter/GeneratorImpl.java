package approximative.query.processing.converter;

import approximative.query.processing.translator.schema.Query;
import groovy.lang.Tuple2;
import oracle.pgql.lang.PgqlException;
import oracle.pgx.api.PgqlResultSet;
import oracle.pgx.api.PgxGraph;
import oracle.pgx.api.PgxResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/24/18.
 */
public class GeneratorImpl implements Generator{
    private static final Logger LOG = LogManager.getLogger("GLOBAL");
    private static final String SELECT_CLAUSE = "SELECT COUNT(*)";
    private static final Tuple2<Integer, Integer> LENGTH_RANGE = new Tuple2<>(2, 6);


    @Override
    public Set<Query> generate(PgxGraph graph) {
        Set<String> labels = getSetLabels(graph);
        Set<Query> queries = new HashSet<>(generateDisjunctionQueries(new ArrayList<>(labels)));

        for (String label: labels) {
            queries.add(new Query(SELECT_CLAUSE, String.format("MATCH () -[:%s]-> ()", label)));
            queries.add(new Query(SELECT_CLAUSE, String.format("MATCH () -/:%s?/-> ()", label)));
            queries.add(new Query(SELECT_CLAUSE, String.format("MATCH () -/:%s*/-> ()", label)));
            queries.add(new Query(SELECT_CLAUSE, String.format("MATCH () -/:%s+/-> ()", label)));
        }

        return queries;
    }

    private Set<Query> generateDisjunctionQueries(List<String> labels) {
        Set<Query> queries = new HashSet<>();

        int worstCase = labels.size() * 10;
        int counter = 0;

        try {
            while (counter < labels.size()) {
                int length = ThreadLocalRandom.current().nextInt(LENGTH_RANGE.getFirst(), LENGTH_RANGE.getSecond());

                Query newQuery = new Query();
                newQuery.setSelect(SELECT_CLAUSE);

                Set<String> labelsOnDisjunction = new HashSet<>();
                int size = 0;
                int iterations = 0;
                while (size < length && iterations < worstCase) {
                    int randomAccess = ThreadLocalRandom.current().nextInt(0, labels.size());
                    if (labelsOnDisjunction.add(labels.get(randomAccess)))
                        size++;

                    iterations++;
                }

                String disjunctionClause = String.format("MATCH () -[:%s]-> ()",
                        labelsOnDisjunction.stream().collect(Collectors.joining("|")));
                newQuery.setMatch(disjunctionClause);

                queries.add(newQuery);
                counter++;

            }

        }catch (IllegalArgumentException e) {
            LOG.warn(e.getMessage(), e);
        }

        return queries;
    }

    private Set<String> getSetLabels(PgxGraph graph) {
        Set<String> labels = new HashSet<>();

        try {
            PgqlResultSet results = graph.queryPgql("SELECT DISTINCT(label(e)) MATCH () -[e]-> ()");
            for (PgxResult result : results)
                labels.add(result.getString(1));
        } catch (PgqlException | ExecutionException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }

        return labels;
    }
}
