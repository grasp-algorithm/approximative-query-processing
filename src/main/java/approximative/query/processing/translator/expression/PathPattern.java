package approximative.query.processing.translator.expression;

import approximative.query.processing.translator.analyzer.Direction;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/15/18.
 */
public abstract class PathPattern {
    protected String expression;
    protected Direction direction;
    protected boolean path;

    PathPattern(String expression, Direction direction) {
        this.expression = expression;
        this.direction = direction;
    }

    public String getExpression() {
        return expression;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isPath() {
        return path;
    }

    public void setPath(boolean path) {
        this.path = path;
    }

    public PathExpression toPathExpression() {
        return (PathExpression)this;
    }

    public Predicate toPredicate() {
        return (Predicate) this;
    }

    public List<String> getLabels() {
        Pattern pattern = Pattern.compile("(:)?\\w+");
        Matcher matcher = pattern.matcher(expression);

        List<String> labels = new ArrayList<>();
        while (matcher.find())
            labels.add(matcher.group(0).replace(":","").replace("|", ""));

        return labels;
    }


    public String getSingleLabel() {
        if (getLabels().size() > 1) throw new UnsupportedOperationException("This expression contains multiple labels");
        return getLabels().get(0);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PathPattern that = (PathPattern) o;

        return new EqualsBuilder()
                .append(path, that.path)
                .append(expression, that.expression)
                .append(direction, that.direction)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(expression)
                .append(direction)
                .append(path)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("expression", expression)
                .append("direction", direction)
                .append("path", path)
                .toString();
    }
}
