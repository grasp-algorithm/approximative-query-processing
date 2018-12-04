package approximative.query.processing.graph;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/18/18.
 */
public class LabelUse {

    private String label;
    private boolean presentOnHNProperties;
    private boolean presentOnCrossEdges;
    private boolean presentOnReachCount;
    private boolean presentOnPathInCount;
    private boolean presentOnPathOutCount;
    private boolean presentOnSumInDegree;
    private boolean presentOnSumOutDegree;

    public LabelUse() {
    }

    public LabelUse(String label, boolean presentOnHNProperties, boolean presentOnCrossEdges,
                    boolean presentOnReachCount, boolean presentOnPathInCount, boolean presentOnPathOutCount) {
        this.label = label;
        this.presentOnHNProperties = presentOnHNProperties;
        this.presentOnCrossEdges = presentOnCrossEdges;
        this.presentOnReachCount = presentOnReachCount;
        this.presentOnPathInCount = presentOnPathInCount;
        this.presentOnPathOutCount = presentOnPathOutCount;
    }

    public String getLabel() {
        return label;
    }

    public LabelUse setLabel(String label) {
        this.label = label;
        return this;
    }

    public boolean isPresentOnHNProperties() {
        return presentOnHNProperties;
    }

    public LabelUse setPresentOnHNProperties(boolean presentOnHNProperties) {
        this.presentOnHNProperties = presentOnHNProperties;
        return this;
    }

    public boolean isPresentOnCrossEdges() {
        return presentOnCrossEdges;
    }

    public LabelUse setPresentOnCrossEdges(boolean presentOnCrossEdges) {
        this.presentOnCrossEdges = presentOnCrossEdges;
        return this;
    }

    public boolean isPresentOnReachCount() {
        return presentOnReachCount;
    }

    public LabelUse setPresentOnReachCount(boolean presentOnReachCount) {
        this.presentOnReachCount = presentOnReachCount;
        return this;
    }

    public boolean isPresentOnPathInCount() {
        return presentOnPathInCount;
    }

    public LabelUse setPresentOnPathInCount(boolean presentOnPathInCount) {
        this.presentOnPathInCount = presentOnPathInCount;
        return this;
    }

    public boolean isPresentOnPathOutCount() {
        return presentOnPathOutCount;
    }

    public LabelUse setPresentOnPathOutCount(boolean presentOnPathOutCount) {
        this.presentOnPathOutCount = presentOnPathOutCount;
        return this;
    }

    public boolean isPresentOnSumInDegree() {
        return presentOnSumInDegree;
    }

    public LabelUse setPresentOnSumInDegree(boolean presentOnSumInDegree) {
        this.presentOnSumInDegree = presentOnSumInDegree;
        return this;
    }

    public boolean isPresentOnSumOutDegree() {
        return presentOnSumOutDegree;
    }

    public LabelUse setPresentOnSumOutDegree(boolean presentOnSumOutDegree) {
        this.presentOnSumOutDegree = presentOnSumOutDegree;
        return this;
    }
}
