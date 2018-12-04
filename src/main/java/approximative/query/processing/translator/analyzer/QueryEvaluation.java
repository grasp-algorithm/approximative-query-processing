package approximative.query.processing.translator.analyzer;

import approximative.query.processing.Main;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.util.PrefixProperty;
import com.google.common.util.concurrent.AtomicDouble;
import oracle.pgql.lang.PgqlException;
import oracle.pgx.api.PgqlResultSet;
import oracle.pgx.api.PgxGraph;
import oracle.pgx.api.PgxResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/21/18.
 */
public class QueryEvaluation {

    private static final Logger LOG = LogManager.getLogger("GLOBAL");

    interface CrossAndInnerEdge {

        Float getLabelParticipation();

        Integer getEdgeWeight();

        Float getPercentageLabel();

        Integer getNumberOfSNs();
    }

    private QueryEvaluation() {
        throw new UnsupportedOperationException("This class couldn't be instantiated");
    }

    public static Double evaluate(PgxGraph summary, long numberOfVertices, Query query) {
        Double queryResult = 0D;
        try {

            PgqlResultSet results;
            if (query.isComputeMin()) {
                long start = System.currentTimeMillis();
                queryResult += computeMinValue(summary, query);
                long end = System.currentTimeMillis();
//                if (Main.DEBUG)
                LOG.debug(String.format("Min result: %s in %s ms", queryResult, (end - start)));

            } else {
                long start = System.currentTimeMillis();
                results = summary.queryPgql(query.getQuery());
                queryResult += results.iterator().next().getDouble(1);
                long end = System.currentTimeMillis();

                LOG.debug(String.format("Query result: %s in %s ms", queryResult, (end - start)));

            }


            if (query.needSumVertices()) {
                long start = System.currentTimeMillis();
                queryResult += numberOfVertices;
                long end = System.currentTimeMillis();
                LOG.debug(String.format("+ Sum of vertices result: %s in %s ms", queryResult, (end - start)));
            }

        } catch (PgqlException | ExecutionException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }

        return queryResult;
    }


    private static double computeMinValue(PgxGraph summary, Query query) throws ExecutionException, InterruptedException {

        PgqlResultSet results = summary.queryPgql(query.getQuery());

        if (Main.DEBUG)
            summary.queryPgql(query.getQuery()).print();

        AtomicDouble sum = new AtomicDouble(0D);

        for (PgxResult result : results) {
            AtomicReference<Float> numberOfInnerEdges = new AtomicReference<>(0F);
            AtomicReference<Float> minPercentage = new AtomicReference<>(1F);

            results.getPgqlResultElements().forEach(elem -> {

                try {
                    if (elem.getVarName().contains(PrefixProperty.NUMBER_OF_INNER_EDGES.toString())) {
                        numberOfInnerEdges.set(result.getFloat(elem.getVarName()));

                    } else {
                        if (minPercentage.get().compareTo(result.getFloat(elem.getVarName())) > 0) {
                            minPercentage.set(result.getFloat(elem.getVarName()));
                        }
                    }
                } catch (PgqlException e) {
                    LOG.error(e.getMessage(), e);
                }
            });

            sum.addAndGet(minPercentage.get() * numberOfInnerEdges.get());
        }

        return sum.get();
    }
}
