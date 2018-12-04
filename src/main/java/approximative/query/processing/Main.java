package approximative.query.processing;

import approximative.query.processing.exceptions.NeedSumOfVerticesException;
import approximative.query.processing.exceptions.PatternNotAllowedException;
import approximative.query.processing.graph.GraphSession;
import approximative.query.processing.graph.Session;
import approximative.query.processing.translator.analyzer.QueryEvaluation;
import approximative.query.processing.translator.analyzer.Translation;
import approximative.query.processing.translator.analyzer.WorkloadLoader;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.util.Util;
import oracle.pgql.lang.PgqlException;
import oracle.pgx.api.PgxGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/13/18.
 */
public class Main {
    public static final boolean DEBUG = false;
    private static final Logger LOG = LogManager.getLogger("GLOBAL");


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, PgqlException {
        disableWarning();

        try (Session session = new GraphSession()) {
            session.initialize();

            session.loadOriginalGraph(args[0] + "edge_file.txt",
                    args[0] + "vertex_file.txt");

//            session.loadOriginalGraph(args[0] + "example-graph.json");

            session.loadSummaryGraph(args[0] + "summary-pgx.json",
                    args[0] + "static-label-use.txt");


            /* The first query take more time that in general, so we perform a query before start the computation of
             * the runtime. */
            session.getOriginalGraph().queryPgql("SELECT COUNT(*) MATCH () -[:l0]-> ()").iterator().next()
                    .getLong(1);

            System.out.println("Number Query Type Length Error NumberOfQueries RuntimeOG RuntimeSG ResultOG ResultSG RuntimeOG RuntimeSG " +
                    "NoPresentOnCrossEdges");

            WorkloadLoader loader = new WorkloadLoader();

            if (args.length == 1 || (args[1].equals("--filter")))
                loader.generateWorkloads(session.getOriginalGraph());
            else
                loader.loadWorkload(Arrays.copyOfRange(args, 1, 2), true);

            // just for runtime-example
//            loader.getWorkload().getQueries().add(new Query("SELECT COUNT(*)", "MATCH () -[:replies]-> ()"));
//            loader.getWorkload().getQueries().add(new Query("SELECT COUNT(*)", "MATCH () -/:moderates?/-> ()"));
//            loader.getWorkload().getQueries().add(new Query("SELECT COUNT(*)", "MATCH () -/:knows+/-> ()"));
//            loader.getWorkload().getQueries().add(new Query("SELECT COUNT(*)", "MATCH () -/:knows*/-> ()"));
//            loader.getWorkload().getQueries().add(new Query("SELECT COUNT(*)", "MATCH () -[:knows|follows]-> ()"));
//            loader.getWorkload().getQueries().add(new Query("SELECT COUNT(*)", "MATCH () <-[:authors]- () -[:replies]-> ()"));


            loader.getWorkload().getQueries().forEach(q -> LOG.debug(q.getQuery().trim()));


            if (args[args.length - 1].equals("--filter")) {
                long beforeVertices = session.getSummaryGraph().getNumVertices();
                long beforeEdges = session.getSummaryGraph().getNumEdges();

                PgxGraph filteredGraph = session.filterSummaryGraph(loader.getLabelsOnWorkload());
                session.setSummaryGraph(filteredGraph);

                long afterVertices = session.getSummaryGraph().getNumVertices();
                long afterEdges = session.getSummaryGraph().getNumEdges();

                LOG.debug(String.format("Filtered applied. Before #edges %s #vertices %s. After #edges %s " +
                        "#vertices %s", beforeVertices, beforeEdges, afterVertices, afterEdges));
            }


            Translation translation = new Translation(session.getLabelUseSet());
            AtomicInteger counter = new AtomicInteger(0);
            loader.getWorkload().getQueries().forEach(query -> {

                try {

                    long start = System.currentTimeMillis();
                    Long resultOriginal = session.getOriginalGraph().queryPgql(query.getQuery())
                            .iterator().next().getLong(1);
                    long runtimeOG = (System.currentTimeMillis() - start);

                    LOG.debug("ORIGINAL GRAPH");
                    LOG.debug("--------------");
                    LOG.debug("Q" + counter.incrementAndGet() + "~ " + query.getQuery().trim());
                    LOG.debug("====>> Result: " + resultOriginal);
                    LOG.debug("Time: " + runtimeOG + " ms");

                    LOG.debug("SUMMARY GRAPH");
                    LOG.debug("-------------");


                    List<Long> iterations = new ArrayList<>();
                    int iteration = 0;

                    int numberOfQueries = 0;
                    Double sum = 0D;
                    float error = 0F;
                    PgxGraph summary = session.filterSummaryGraph(Util.getLabels(query.getMatch()));
                    while (iteration < 6) {
                        iteration++;

                        try {
                            sum = 0D;

                            List<Query> queries = translation.getTranslation(query);
                            Double multiply = 1D;
                            boolean activateMultiply = false;
                            numberOfQueries = queries.size();

                            start = System.currentTimeMillis();

                            for (Query q : queries) {
                                LOG.debug(q.getQuery().trim());
                                Double param = QueryEvaluation.evaluate(summary, session.getNumVertices(), q);

                                if (q.needMultiply()) {

                                    if (q.isComputeConcatenation() && multiply > 1) {
                                        sum += multiply;
                                        multiply = 1D;
                                    }

                                    multiply *= param;
                                    activateMultiply = true;

                                } else if (activateMultiply) {

                                    sum += multiply;
                                    multiply = 1D;
                                    activateMultiply = false;

                                } else {
                                    sum += param;
                                    activateMultiply = false;
                                }
                            }

                            if (activateMultiply && multiply != 1)
                                sum += multiply;

                        } catch (NeedSumOfVerticesException e) {
                            sum += (double) session.getNumVertices();
                        }

                        long runtimeSummary = (System.currentTimeMillis() - start);
                        iterations.add(runtimeSummary);
                        error = computeError(resultOriginal, sum.floatValue());
                    }

                    iterations.remove(0);
                    printResume(query, counter.get(), error, numberOfQueries, runtimeOG, iterations, resultOriginal,
                            sum.longValue(), session);

                } catch (PgqlException | ExecutionException | InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                } catch (PatternNotAllowedException e) {
                    LOG.error((char) 27 + "[31m" + "Error: " + e.getMessage());
                    LOG.error((char) 27 + "[30m");
                }
            });

        }

    }


    private static void printResume(Query query, int counter, float error, int numberOfQueries, long runtimeOG,
                                    List<Long> runtimeSGs, long resultOriginal, long resultSummary, Session session) {

        String type = Util.getType(query);
        int length = Util.getLength(query);
        List<String> labels = Util.getLabels(query.getMatch());
        boolean noPresentOnCrossEdges = labels.stream().anyMatch(l -> session.getLabelUseSet().stream()
                .anyMatch(lu -> lu.getLabel().equals(l) && !lu.isPresentOnCrossEdges()));

        double runtimeSG = runtimeSGs.stream().mapToLong(v -> v).average().orElse(0);
        LOG.debug("RUNTIMEs :: " + runtimeSGs.stream().map(String::valueOf).collect(Collectors.joining(" ms, ")));

        float gain = computeGain(runtimeOG, (float) runtimeSG);
        if (error > 10) {
            System.out.println((char) 27 + "[31m" + "Q" + counter + " \"" + query.getQuery().trim() + "\" \"" + type
                    + "\" " + length + " " + error + "% " + numberOfQueries + " " + runtimeOG + " " + runtimeSG + " " +
                    resultOriginal + " " + resultSummary + " " + runtimeOG + " " + runtimeSG + " " + noPresentOnCrossEdges);
        } else {
            System.out.println((char) 27 + "[30m" + "Q" + counter + " \"" + query.getQuery().trim() + "\" \"" + type
                    + "\" " + length + " " + error + "% " + numberOfQueries + " " + runtimeOG + " " + runtimeSG + " " +
                    resultOriginal + " " + resultSummary + " " + runtimeOG + " " + runtimeSG
                    + " " + noPresentOnCrossEdges);
        }

    }


    private static float computeGain(float runtimeOG, float runtimeSG) {
        return (runtimeOG - runtimeSG) / Math.max(runtimeOG, runtimeSG) * 100;
    }

    private static float computeError(float resultOriginal, float resultSummary) {

        float accuracy = Math.min(resultSummary, resultOriginal) / Math.max(resultSummary, resultOriginal) * 100;

        if (resultSummary == resultOriginal)
            accuracy = 100;

        LOG.debug("====>> Result: " + resultSummary);
        LOG.debug((char) 27 + "[31m" + "Accuracy: " + accuracy + "%");
        LOG.debug((char) 27 + "[30m" + "=================================================");

        return 100 - accuracy;
    }

    private static void disableWarning() {
        System.err.close();
        System.setErr(System.out);
    }
}
