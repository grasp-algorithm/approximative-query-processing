package approximative.query.processing.translator.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/14/18.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "queries"
})
public class Workload {
    @JsonProperty("queries")
    private Set<Query> queries = null;

    public Workload() {
        this.queries = new HashSet<>();
    }

    @JsonProperty("queries")
    public Set<Query> getQueries() {
        return queries;
    }

    @JsonProperty("queries")
    public void setQueries(Set<Query> queries) {
        this.queries = queries;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("queries", queries).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(queries).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Workload)) {
            return false;
        }
        Workload rhs = ((Workload) other);
        return new EqualsBuilder().append(queries, rhs.queries).isEquals();
    }

}