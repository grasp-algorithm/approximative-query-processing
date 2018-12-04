package approximative.query.processing.util;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/14/18.
 */
public enum PrefixProperty {

    PERCENTAGE("PERCENTAGE"),
    EDGE_RATIO("EDGE_RATIO"),
    EDGE_WEIGHT("EDGE_WEIGHT"),
    NODE_WEIGHT_EVALUATION("NUMBER_OF_NODES_INSIDE_OF_SN"),
    NODE_WEIGHT_FREQUENCY("NUMBER_OF_SN_INSIDE_OF_HN"),
    NODE_WEIGHT_EVALUATION_AVG("AVG_WEIGHT_ON_SN"),
    REACHABILITY_COUNT("REACH_NUMBER_OF_INNER_PATHS"),
    PATH_OUT("REACH_PATH_OUT_BY_LABEL"),
    REACH_PATH_IN_BY_LABEL("REACH_PATH_IN_BY_LABEL"),
    NUMBER_OF_INNER_EDGES("NUMBER_OF_INNER_EDGES"),
    PARTICIPATION_LABEL("PARTICIPATION_LABEL"),
    TRAVERSAL_FRONTIERS("TRAVERSAL_FRONTIERS");


    private String description;

    PrefixProperty(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
