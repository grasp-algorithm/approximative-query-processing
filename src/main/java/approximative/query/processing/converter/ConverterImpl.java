package approximative.query.processing.converter;

import approximative.query.processing.translator.analyzer.syntactic.analysis.KleeneStarAnalyzer;
import approximative.query.processing.translator.analyzer.syntactic.analysis.SingleLabelsAnalyzer;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.translator.schema.Workload;
import approximative.query.processing.util.Util;
import com.cloudera.org.codehaus.jackson.map.ObjectMapper;
import groovy.lang.Tuple2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/19/18.
 */
public class ConverterImpl implements  Converter {
    private static final Logger LOG = LogManager.getLogger("GLOBAL");
    private static final String SELECT_CLAUSE = "SELECT COUNT(*)";

    @Override
    public Set<Query> convert(String workloadDirectory, boolean generateDisjunction) throws IOException {
        Set<Query> queries = new HashSet<>();

        File folder = new File(workloadDirectory);
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && file.getName().endsWith("cypher")) {
                String content = Files.readAllLines(file.toPath()).get(0);

                Query query = new Query();
                query.setSelect(SELECT_CLAUSE);
                query.setMatch(replaceLabels(content.substring(content.indexOf("MATCH"),
                        content.indexOf("RETURN"))));

                Pattern kleeneStarPattern = Pattern.compile("\\[:\\w+\\*]");
                Matcher kleeneStarMatcher = kleeneStarPattern.matcher(query.getMatch());
                if (kleeneStarMatcher.find()) {
                    query.setMatch(query.getMatch().replace("[", "/")
                            .replace("]", "/"));
                }

                queries.add(query);
            } else if (file.getName().contains("workload")) {
                ObjectMapper mapper = new ObjectMapper();
                Workload workload = mapper.readValue(file, Workload.class);
                queries.addAll(workload.getQueries());
            }
        }

        return check(queries, generateDisjunction);
    }

    /**
     * This methods adds the queries than gmark doesn't use.
     * But it takes as an input the queries of gmark in order to respect the
     *
     * @param queries the gmark queries
     * @param generateDisjunction option to generate disjunction queries
     * @return the set of queries of gmark plus the kleene plus, optional and disjunction queries
     */
    private Set<Query> check(Set<Query> queries, boolean generateDisjunction) {
        Set<Query> querySet = new HashSet<>();
        Set<Query> singleLabelQueries = new HashSet<>();

        for (Query query: queries) {
            if (new SingleLabelsAnalyzer(query, null).verify()) {
                querySet.add(addOptionalQuery(query));
                singleLabelQueries.add(query);
            } else if (new KleeneStarAnalyzer( query, null).verify()) {
                querySet.add(addKleenePlusQuery(query));
            }
//            else if (new ConcatenationAnalyzer(query, null).verify()) {
//                List<String> labels = Util.getLabels(query.getMatch());
//
//                while (labels.size() > 1) {
//                    System.out.print(String.format("\"%s:%s\",", labels.get(0), labels.get(1)));
//                    labels.remove(0);
//                }
//            }


        }

        queries.addAll(querySet);
        if (generateDisjunction)
            queries.addAll(generateDisjunctionQueries(new ArrayList<>(singleLabelQueries),
                    new Tuple2<>(2, 6), singleLabelQueries.size() * 2));
        return queries;
    }

    /**
     * This method generates queries with disjunction clause.
     *
     * @param singleLabelQueries the queries with single label
     * @param range the values min and max to the length of the disjunction clauses
     * @param numberOfQueries the number of disjunction queries that we want
     * @return the set of disjunction queries.
     */
    private Set<Query> generateDisjunctionQueries(List<Query> singleLabelQueries, Tuple2<Integer, Integer> range,
                                                  int numberOfQueries) {
        Set<Query> queries = new HashSet<>();
        int counter = 0;

        int worstCase = numberOfQueries * 10;

        try {
            while (counter < numberOfQueries) {
                int length = ThreadLocalRandom.current().nextInt(range.getFirst(), range.getSecond());

                Query newQuery = new Query();
                newQuery.setSelect(SELECT_CLAUSE);

                Set<String> labels = new HashSet<>();
                int size = 0;
                int iterations = 0;
                while (size < length && iterations < worstCase) {
                    int randomAccess = ThreadLocalRandom.current().nextInt(0, singleLabelQueries.size());
                    if (labels.add(Util.getLabels(singleLabelQueries.get(randomAccess).getMatch()).get(0)))
                        size++;

                    iterations++;
                }

                String disjunctionClause = String.format("MATCH () -[:%s]-> ()",
                        labels.stream().collect(Collectors.joining("|")));
                newQuery.setMatch(disjunctionClause);

                queries.add(newQuery);
                counter++;
            }
        }catch (IllegalArgumentException e) {
            LOG.warn(e.getMessage(), e);
        }

        return queries;
    }


    private Query addKleenePlusQuery(Query query) {
        Query newQuery = new Query(query);
        String newMatch = newQuery.getMatch();

        String singleLabel = Util.getLabels(newMatch).get(0);
        Pattern pattern = Pattern.compile("/:\\w+\\*/");
        Matcher matcher = pattern.matcher(newMatch);

        while (matcher.find())
            newMatch = newMatch.replace(matcher.group(0), String.format("/:%s+/", singleLabel));

        newQuery.setMatch(newMatch);

        return newQuery;
    }

    private Query addOptionalQuery(Query query) {
        Query newQuery = new Query(query);
        String newMatch = newQuery.getMatch();

        String singleLabel = Util.getLabels(newMatch).get(0);
        Pattern pattern = Pattern.compile("\\[:\\w+]");
        Matcher matcher = pattern.matcher(newMatch);

        while (matcher.find())
            newMatch = newMatch.replace(matcher.group(0), String.format("/:%s?/", singleLabel));

        newQuery.setMatch(newMatch);

        return newQuery;
    }

    /**
     * The gmark workload generates the labels with a prefix "p". we want to remove them.
     *
     * @param matchClause the match clause in the cypher translation of gmark.
     * @return the new match clause without the prefix "p"
     */
    private String replaceLabels(String matchClause) {

        String newMatchClause = matchClause;

        List<String> labels = Util.getLabels(matchClause);
        for (String label: labels) {
            newMatchClause = newMatchClause.replace(label, label.replaceFirst("p", ""));
        }

        return newMatchClause;
    }


}
