package approximative.query.processing.translator.analyzer.syntactic.control;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/21/18.
 */
public class LabelBySN {

    private Integer numberOfSNs;
    private Float avgWeightOfSNs;
    private Float percentageOfLabel;

    public LabelBySN(Integer numberOfSNs, Float avgWeightOfSNs, Float percentageOfLabel) {
        this.numberOfSNs = numberOfSNs;
        this.avgWeightOfSNs = avgWeightOfSNs;
        this.percentageOfLabel = percentageOfLabel;
    }

    public Integer getNumberOfSNs() {
        return numberOfSNs;
    }

    public void setNumberOfSNs(Integer numberOfSNs) {
        this.numberOfSNs = numberOfSNs;
    }

    public Float getAvgWeightOfSNs() {
        return avgWeightOfSNs;
    }

    public void setAvgWeightOfSNs(Float avgWeightOfSNs) {
        this.avgWeightOfSNs = avgWeightOfSNs;
    }

    public Float getPercentageOfLabel() {
        return percentageOfLabel;
    }

    public void setPercentageOfLabel(Float percentageOfLabel) {
        this.percentageOfLabel = percentageOfLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        LabelBySN labelBySN = (LabelBySN) o;

        return new EqualsBuilder()
                .append(numberOfSNs, labelBySN.numberOfSNs)
                .append(avgWeightOfSNs, labelBySN.avgWeightOfSNs)
                .append(percentageOfLabel, labelBySN.percentageOfLabel)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(numberOfSNs)
                .append(avgWeightOfSNs)
                .append(percentageOfLabel)
                .toHashCode();
    }
}
