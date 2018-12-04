package approximative.query.processing.translator.analyzer.syntactic.analysis;

import approximative.query.processing.exceptions.NotPropertyFound;
import approximative.query.processing.graph.LabelUse;
import approximative.query.processing.translator.expression.PathPattern;
import approximative.query.processing.translator.schema.Query;
import approximative.query.processing.translator.analyzer.Direction;
import approximative.query.processing.util.Constants;
import approximative.query.processing.util.PrefixProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 7/6/18.
 */
public class ConcatenationAnalyzer extends SyntacticAnalyzer implements Analyzer {
    private static final Logger LOG = LogManager.getLogger("GLOBAL");
    private static final String MATCH_CLAUSE = "MATCH ";

    public ConcatenationAnalyzer(Query inputQuery, Set<LabelUse> labelUseSet) {
        super(inputQuery, labelUseSet);
    }

    //todo
    @Override
    public boolean verify() {
        return pathPatterns.size() > 1 && pathPatterns.stream().noneMatch(PathPattern::isPath);
    }


    @Override
    public List<Query> translation() {
        List<Query> queries = new ArrayList<>();

        StringBuilder selectInsideHN = new StringBuilder("SELECT ");
        StringBuilder matchInsideHN = new StringBuilder(MATCH_CLAUSE);
        StringBuilder whereInsideHN = new StringBuilder("WHERE ");

        boolean labelsInsideOfHN = true;
        boolean labelsOnCrossEdges = true;

        int counter = 0;

        for (PathPattern pathPattern : pathPatterns) {
            List<String> labelsOnPredicate = pathPattern.getLabels();

            for (String label : labelsOnPredicate) {

                labelsInsideOfHN = labelsInsideOfHN && labelUseSet.stream().anyMatch(lu -> lu.getLabel().equals(label) &&
                        lu.isPresentOnHNProperties());

                if (labelsInsideOfHN)
                    computeEstimationInsideHN(Arrays.asList(selectInsideHN, matchInsideHN, whereInsideHN), label,
                            counter, pathPatterns.size());

                labelsOnCrossEdges = labelsOnCrossEdges && labelUseSet.stream().anyMatch(lu -> lu.getLabel().equals(label) &&
                        lu.isPresentOnCrossEdges());

            }

            counter++;
        }

        if (labelsInsideOfHN)
            queries.add(new Query(selectInsideHN.toString(), matchInsideHN.toString(), whereInsideHN.toString())
                    .setComputeMin(true));


        try {
            queries.add(computeQueriesOnHNAndCE(new ArrayList<>(pathPatterns), true));
        } catch (NotPropertyFound e) {
            LOG.debug(e.getMessage());
        }
        try {
            queries.add(computeQueriesOnHNAndCE(new ArrayList<>(pathPatterns), false));
        } catch (NotPropertyFound e) {
            LOG.debug(e.getMessage());
        }

        if (labelsOnCrossEdges) {
            try {
                queries.add(buildCrossEdgeQuery(pathPatterns));
            } catch (NotPropertyFound e) {
                LOG.debug(e.getMessage());
            }
        }

        return queries;
    }


    /**
     * @param pathPatterns
     *
     * @return
     */
    private Query computeQueriesOnHNAndCE(List<PathPattern> pathPatterns, boolean firstHN) throws NotPropertyFound {

        Query query = new Query();
        query.setSelect("SELECT SUM(");
        query.setMatch("");
        query.setWhere("WHERE ");

        List<String> toMultiply = new ArrayList<>();
        List<String> toDivide = new ArrayList<>();
        List<String> whereClause = new ArrayList<>();

        int varCounter = 0;
        boolean insideHN = firstHN;
        PathPattern before = null;

        for (PathPattern current : pathPatterns) {

            String nodeVar = VAR_NODE_NAME.concat(String.valueOf(varCounter));
            String edgeVar = VAR_EDGE_NAME.concat(String.valueOf(varCounter));

            if (insideHN) {
                PathPattern crossLabel;
                if (varCounter == 0)
                    crossLabel = pathPatterns.get(1);
                else
                    crossLabel = before;

                query.setMatch(query.getMatch().concat(String.format(" (%s) ", nodeVar)));

                toMultiply.add(String.format("%1$s.%2$s", nodeVar, getLabelParticipation(current,
                        crossLabel, varCounter == 0)));

                toDivide.add(String.format("%1$s.%2$s * %1$s.%3$s", nodeVar, PrefixProperty.PERCENTAGE.toString()
                                .concat(Constants.SEPARATOR).concat(current.getSingleLabel()),
                        PrefixProperty.NODE_WEIGHT_FREQUENCY.toString()));

                whereClause.add(String.format("%1$s.%2$s > 0", nodeVar,
                        PrefixProperty.PERCENTAGE.toString().concat(Constants.SEPARATOR)
                                .concat(current.getSingleLabel())));
            } else {
                query.setMatch(query.getMatch().concat(getCrossEdgeVar(current, edgeVar)));
                if (varCounter < 2)
                    toMultiply.add(String.format("%1$s.%2$s", edgeVar, PrefixProperty.EDGE_WEIGHT.toString()));
                else
                    toMultiply.add(String.format("%1$s.%2$s", edgeVar, PrefixProperty.EDGE_RATIO.toString()));
            }

            before = current;
            insideHN = !insideHN;
            varCounter++;
        }

        if (!firstHN)
            query.setMatch("()".concat(query.getMatch()));

        query.setMatch(MATCH_CLAUSE.concat(query.getMatch()));

        if (insideHN)
            query.setMatch(query.getMatch().concat(" ()"));

        query.setSelect(query.getSelect().concat("(").concat(toMultiply.stream().collect(Collectors.joining(" * ")))
                .concat(") / (").concat(toDivide.stream().collect(Collectors.joining(" * "))).concat(") )"));
        query.setWhere(query.getWhere().concat(whereClause.stream().collect(Collectors.joining(" AND "))));

        return query;
    }

    private String getCrossEdgeVar(PathPattern pathPattern, String edgeVar) throws NotPropertyFound {
        String crossEdge = "";

        if (pathPattern.getDirection().equals(Direction.RIGHT))
            crossEdge = String.format("-[%s:%s]->", edgeVar, pathPattern.getSingleLabel());
        else
            crossEdge = String.format("<-[%s:%s]-", edgeVar, pathPattern.getSingleLabel());

        if (labelUseSet.stream().anyMatch(lu -> lu.getLabel().equals(pathPattern.getSingleLabel()) && lu.isPresentOnCrossEdges()))
            return crossEdge;

        throw new NotPropertyFound(String.format("The property %s has not been founded", crossEdge));

    }

    //fixme it's driven me crazy
    private String getLabelParticipation(PathPattern hyperNode, PathPattern crossEdge, boolean firstCrossEdge) throws NotPropertyFound {
        String labelParticipationOrientation;

        if (hyperNode.getDirection().equals(Direction.RIGHT)) {
            if (firstCrossEdge) {
                if (crossEdge.getDirection().equals(Direction.RIGHT)) {
                    labelParticipationOrientation = "_OUT_IN";
                } else {
                    labelParticipationOrientation = "_IN_IN";
                }
            } else {
                if (crossEdge.getDirection().equals(Direction.RIGHT)) {
                    labelParticipationOrientation = "_IN_OUT";
                } else {
                    labelParticipationOrientation = "_OUT_OUT";
                }
            }

        } else {
            if (firstCrossEdge) {
                if (crossEdge.getDirection().equals(Direction.RIGHT)) {
                    labelParticipationOrientation = "_OUT_OUT";
                } else {
                    labelParticipationOrientation = "_IN_OUT";
                }
            } else {
                if (crossEdge.getDirection().equals(Direction.RIGHT)) {
                    labelParticipationOrientation = "_IN_IN";
                } else {
                    labelParticipationOrientation = "_OUT_IN";
                }
            }
        }

        labelParticipationOrientation = labelParticipationOrientation.concat(Constants.SEPARATOR)
                .concat(crossEdge.getSingleLabel())
                .concat(Constants.SEPARATOR)
                .concat(hyperNode.getSingleLabel());

        String property = PrefixProperty.PARTICIPATION_LABEL.toString().concat(labelParticipationOrientation);
        if (checkIfPropertyExists(property))
            return property;

        throw new NotPropertyFound(String.format("The property %s has not been founded", property));

    }


    private boolean checkIfPropertyExists(String property) {
        return labelUseSet.stream().anyMatch(lu -> lu.getLabel().equals(property));
    }


    /**
     * @param stmts
     * @param label
     * @param counter
     * @param pathPatternSize
     */
    private void computeEstimationInsideHN(List<StringBuilder> stmts, String label, int counter,
                                           int pathPatternSize) {
        StringBuilder select = stmts.get(0);
        StringBuilder match = stmts.get(1);
        StringBuilder where = stmts.get(2);

        if (counter == 0) {
            select.append(String.format("%s.%s, ", VAR_NODE_NAME, PrefixProperty.NUMBER_OF_INNER_EDGES.toString()));
            match.append(String.format("(%s)", VAR_NODE_NAME));
        }

        if (!select.toString().contains(PrefixProperty.PERCENTAGE.toString()
                .concat(Constants.SEPARATOR).concat(label))) {
            select.append(String.format("%s.%s", VAR_NODE_NAME, PrefixProperty.PERCENTAGE.toString()
                    .concat(Constants.SEPARATOR).concat(label)));
            where.append(String.format("%s.%s > 0", VAR_NODE_NAME, PrefixProperty.PERCENTAGE.toString()
                    .concat(Constants.SEPARATOR).concat(label)));

            if (++counter < pathPatternSize) {
                select.append(", ");
                where.append(" AND ");
            }
        } else {

            if (++counter == pathPatternSize) {
                select.replace(select.lastIndexOf(", "), select.length(), "");
                where.replace(where.lastIndexOf("AND "), where.length(), "");
            }
        }

    }


    private String getTraversalFrontierProperty(PathPattern before, PathPattern after) throws NotPropertyFound {
        String direction1 = "";
        String direction2 = "";

        if (before.getDirection().equals(Direction.LEFT))
            direction1 = direction1.concat("_OUT");
        else
            direction1 = direction1.concat("_IN");

        if (after.getDirection().equals(Direction.LEFT))
            direction2 = direction2.concat("_IN");
        else
            direction2 = direction2.concat("_OUT");

        String direction;

            direction = direction1.concat(direction2)
                    .concat(Constants.SEPARATOR).concat(before.getSingleLabel())
                    .concat(Constants.SEPARATOR).concat(after.getSingleLabel());
        String property = PrefixProperty.TRAVERSAL_FRONTIERS.toString().concat(direction);

        if (labelUseSet.stream().noneMatch(lu -> lu.getLabel().equals(property))) {

            direction = direction2.concat(direction1)
                    .concat(Constants.SEPARATOR).concat(after.getSingleLabel())
                    .concat(Constants.SEPARATOR).concat(before.getSingleLabel());

            String property2 = PrefixProperty.TRAVERSAL_FRONTIERS.toString().concat(direction);

            if (labelUseSet.stream().noneMatch(lu -> lu.getLabel().equals(property2)))
                throw new NotPropertyFound(String.format("The property %s has not been founded", property2));

            return property2;
        }

        return property;
    }


    private Query buildCrossEdgeQuery(List<PathPattern> pathPatternList) throws NotPropertyFound {

        Query query = new Query();
        query.setSelect("SELECT SUM (");
        query.setMatch(MATCH_CLAUSE);
        query.setWhere("WHERE ");

        int varCounter = 0;
        PathPattern before = pathPatternList.get(0);
        pathPatternList.remove(0);

        boolean inHN = true;
        List<String> selectClause = new ArrayList<>();
        List<String> whereClause = new ArrayList<>();

        //second , third, ...
        for (PathPattern current : pathPatternList) {

            String edgeVar = VAR_EDGE_NAME.concat(String.valueOf(varCounter));
            String nodeVar = VAR_NODE_NAME.concat(String.valueOf(varCounter));

            if (inHN) {
                String property = getTraversalFrontierProperty(before, current);

                selectClause.add(String.format("%1$s.%2$s", nodeVar, property));
                query.setMatch(query.getMatch().concat(String.format("(%s)", nodeVar)));
                whereClause.add(String.format("%1$s.%2$s > 0", nodeVar, property));

            } else {
                selectClause.add(String.format("%1$s.%2$s", edgeVar, PrefixProperty.EDGE_RATIO.toString()));
                query.setMatch(query.getMatch().concat(getCrossEdgeVar(current, edgeVar)));

                before = current;
            }

            inHN = !inHN;
            varCounter++;

        }

        if (inHN)
            query.setMatch(query.getMatch().concat("()"));

        query.setSelect(query.getSelect()
                .concat(selectClause.stream().collect(Collectors.joining(" * "))).concat(")"));
        query.setWhere(query.getWhere().concat(whereClause.stream().collect(Collectors.joining(" AND "))));


        return query;
    }


}
