package approximative.query.processing.translator.schema;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/14/18.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
       "path", "select", "match", "where"
})
public class Query {
    @JsonProperty("path")
    private String path;
    @JsonProperty("select")
    private String select;
    @JsonProperty("match")
    private String match;
    @JsonProperty("where")
    private String where;

    @JsonIgnore
    private boolean sumVertices;
    @JsonIgnore
    private boolean computeMin;
    @JsonIgnore
    private boolean computeConcatenation;
    @JsonIgnore
    private boolean multiply;

    public Query() {
        this.path = "";
        this.select = "";
        this.match = "";
        this.where = "";
    }

    public Query(String select, String match) {
        Objects.requireNonNull(select);
        Objects.requireNonNull(match);
        this.path = "";
        this.select = select;
        this.match = match;
        this.where = "";
    }

    public Query(String select, String match, String where) {
        Objects.requireNonNull(select);
        Objects.requireNonNull(match);
        Objects.requireNonNull(where);
        this.path = "";
        this.select = select;
        this.match = match;
        this.where = where;
    }

    public Query(String path, String select, String match, String where) {
        this.path = path;
        this.select = select;
        this.match = match;
        this.where = where;
    }


    public Query(Query query) {
        this.path = query.getPath();
        this.select = query.getSelect();
        this.match = query.getMatch();
        this.where = query.getWhere();
        this.sumVertices = query.needSumVertices();
        this.computeConcatenation = query.isComputeConcatenation();
        this.computeMin = query.isComputeMin();
        this.multiply = query.needMultiply();
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("select")
    public String getSelect() {
        return select;
    }

    @JsonProperty("select")
    public void setSelect(String select) {
        Objects.requireNonNull(where);
        this.select = select;
    }

    @JsonProperty("match")
    public String getMatch() {
        return match;
    }

    @JsonProperty("match")
    public void setMatch(String match) {
        Objects.requireNonNull(match);
        this.match = match;
    }

    @JsonProperty("where")
    public String getWhere() {
        return where;
    }

    @JsonProperty("where")
    public void setWhere(String where) {
        Objects.requireNonNull(where);
        this.where = where;
    }

    @JsonIgnore
    public boolean needSumVertices() {
        return sumVertices;
    }

    @JsonIgnore
    public Query setSumVertices(boolean sumVertices) {
        this.sumVertices = sumVertices;
        return this;
    }

    @JsonIgnore
    public boolean needMultiply() {
        return multiply;
    }

    @JsonIgnore
    public Query setMultiply(boolean multiply) {
        this.multiply = multiply;
        return this;
    }

    @JsonIgnore
    public boolean isComputeMin() {
        return computeMin;
    }

    @JsonIgnore
    public Query setComputeMin(boolean computeMin) {
        this.computeMin = computeMin;
        return this;
    }

    @JsonIgnore
    public boolean isComputeConcatenation() {
        return computeConcatenation;
    }

    @JsonIgnore
    public Query setComputeConcatenation(boolean computeConcatenation) {
        this.computeConcatenation = computeConcatenation;
        return this;
    }

    public String getQuery() {
        return path.concat(" ").concat(select).concat(" ").concat(match).concat(" ").concat(where);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("path", path)
                .append("select", select)
                .append("match", match)
                .append("where", where)
                .toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(path)
                .append(select)
                .append(match)
                .append(where)
                .toHashCode();
    }

    @Override
    public boolean equals(Object other) {

        if (other == this)
            return true;

        if (!(other instanceof Query))
            return false;

        Query rhs = ((Query) other);
        return new EqualsBuilder()
                .append(path, rhs.path)
                .append(select, rhs.select)
                .append(match, rhs.match)
                .append(where, rhs.where)
                .isEquals();
    }
}