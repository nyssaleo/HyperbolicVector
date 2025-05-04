package com.hypervector.index.common;

import com.hypervector.storage.common.VectorRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Expression for filtering vector records based on metadata.
 * Supports basic logical operations and metadata field comparisons.
 */
public abstract class FilterExpression {

    /**
     * Evaluate the expression against a vector record.
     *
     * @param record The vector record to check
     * @return true if the record matches the filter
     */
    public abstract boolean evaluate(VectorRecord record);

    /**
     * Create a logical AND of this expression with another.
     *
     * @param other The other expression
     * @return A new AND expression
     */
    public FilterExpression and(FilterExpression other) {
        return new AndExpression(this, other);
    }

    /**
     * Create a logical OR of this expression with another.
     *
     * @param other The other expression
     * @return A new OR expression
     */
    public FilterExpression or(FilterExpression other) {
        return new OrExpression(this, other);
    }

    /**
     * Create a logical NOT of this expression.
     *
     * @return A new NOT expression
     */
    public FilterExpression not() {
        return new NotExpression(this);
    }

    /**
     * Create a field equals value comparison.
     *
     * @param field Field name
     * @param value Expected value
     * @return A new equals expression
     */
    public static FilterExpression eq(String field, Object value) {
        return new FieldComparisonExpression(field, value, Object::equals);
    }

    /**
     * Create a field not equals value comparison.
     *
     * @param field Field name
     * @param value Value to compare against
     * @return A new not equals expression
     */
    public static FilterExpression ne(String field, Object value) {
        return new FieldComparisonExpression(field, value, (a, b) -> !a.equals(b));
    }

    /**
     * Create a numeric greater than comparison.
     *
     * @param field Field name
     * @param value Value to compare against
     * @return A new greater than expression
     */
    public static FilterExpression gt(String field, Number value) {
        return new FieldComparisonExpression(field, value, (a, b) -> {
            if (!(a instanceof Number)) return false;
            return ((Number) a).doubleValue() > ((Number) b).doubleValue();
        });
    }

    /**
     * Create a numeric less than comparison.
     *
     * @param field Field name
     * @param value Value to compare against
     * @return A new less than expression
     */
    public static FilterExpression lt(String field, Number value) {
        return new FieldComparisonExpression(field, value, (a, b) -> {
            if (!(a instanceof Number)) return false;
            return ((Number) a).doubleValue() < ((Number) b).doubleValue();
        });
    }

    /**
     * Create a field contains value comparison for strings.
     *
     * @param field Field name
     * @param value Substring to check for
     * @return A new contains expression
     */
    public static FilterExpression contains(String field, String value) {
        return new FieldComparisonExpression(field, value, (a, b) -> {
            if (!(a instanceof String)) return false;
            return ((String) a).contains((String) b);
        });
    }

    /**
     * Create a field in collection comparison.
     *
     * @param field Field name
     * @param values Collection of possible values
     * @return A new in expression
     */
    public static FilterExpression in(String field, List<?> values) {
        return new FieldComparisonExpression(field, values, (a, b) -> {
            if (!(b instanceof List)) return false;
            return ((List<?>) b).contains(a);
        });
    }

    /**
     * Logical AND of two expressions.
     */
    private static class AndExpression extends FilterExpression {
        private final FilterExpression left;
        private final FilterExpression right;

        public AndExpression(FilterExpression left, FilterExpression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean evaluate(VectorRecord record) {
            return left.evaluate(record) && right.evaluate(record);
        }
    }

    /**
     * Logical OR of two expressions.
     */
    private static class OrExpression extends FilterExpression {
        private final FilterExpression left;
        private final FilterExpression right;

        public OrExpression(FilterExpression left, FilterExpression right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean evaluate(VectorRecord record) {
            return left.evaluate(record) || right.evaluate(record);
        }
    }

    /**
     * Logical NOT of an expression.
     */
    private static class NotExpression extends FilterExpression {
        private final FilterExpression expression;

        public NotExpression(FilterExpression expression) {
            this.expression = expression;
        }

        @Override
        public boolean evaluate(VectorRecord record) {
            return !expression.evaluate(record);
        }
    }

    /**
     * Field comparison expression.
     */
    private static class FieldComparisonExpression extends FilterExpression {
        private final String field;
        private final Object value;
        private final BiPredicate<Object, Object> comparison;

        public FieldComparisonExpression(String field, Object value, BiPredicate<Object, Object> comparison) {
            this.field = field;
            this.value = value;
            this.comparison = comparison;
        }

        @Override
        public boolean evaluate(VectorRecord record) {
            Map<String, Object> metadata = record.getMetadata();
            if (!metadata.containsKey(field)) {
                return false;
            }

            Object fieldValue = metadata.get(field);
            return comparison.test(fieldValue, value);
        }
    }

    /**
     * Simple functional interface for comparing two objects.
     */
    private interface BiPredicate<T, U> {
        boolean test(T t, U u);
    }
}