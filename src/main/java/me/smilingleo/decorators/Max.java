package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.WithFieldArg;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.StringUtils;

import me.smilingleo.utils.Asserts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Max takes in a list, return a value.
 */
public final class Max implements Decorator, WithFieldArg, ReturningScalar {

    private static final String NAME = "Max";
    private String fieldName;

    private Max(String fieldName) {
        this.fieldName = fieldName;
    }

    public static Max parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        Asserts.assertTrue(arguments.size() == 1, ErrorCode.InvalidFunctionArgument,
                format("Function %s takes only one argument.", NAME));
        Asserts.assertTrue(!arguments.get(0).contains("."), ErrorCode.InvalidFunctionArgument,
                format("Function %s doesn't support dotted object path.", NAME));
        return new Max(arguments.get(0));
    }

    @Override
    public Object evaluate(Object input) {
        if (input == null) {
            return null;
        }
        Asserts.assertType(input, List.class, format("Function %s expects a List type input, but it receives a %s.", NAME,
                input.getClass().getSimpleName()));
        if (((List) input).isEmpty()) {
            return null;
        }
        Object headObj = ((List) input).get(0);
        Asserts.assertType(headObj, Map.class,
                format("Function %s expects an object List type input, but it receives a list of %s", NAME,
                        headObj.getClass().getSimpleName()));
        List<Map<String, Object>> list = new ArrayList<>((List) input);

        Object max = ((Map<String, Object>) headObj).get(fieldName);
        for (Map<String, Object> item : list) {
            Object value = item.get(fieldName);
            int cmp = Objects.compare(max, value, (o1, o2) -> {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null && o2 != null) {
                    return -1;
                }
                if (o1 != null && o2 == null) {
                    return 1;
                }
                // Max can only handle String, Number, Boolean.
                if (o1 instanceof String && o2 instanceof String) {
                    return ((String) o1).compareTo((String) o2);
                }
                if (o1 instanceof Number && o2 instanceof Number) {
                    return Double.compare(((Number) o1).doubleValue(), ((Number) o2).doubleValue());
                }
                if (o1 instanceof Boolean && o2 instanceof Boolean) {
                    return Boolean.compare((Boolean) o1, (Boolean) o2);
                }
                throw new ValidationException(ErrorCode.InvalidFunctionArgument, format("%s is not comparable to %2",
                        o1.getClass().getSimpleName(), o2.getClass().getSimpleName()));
            });
            if (cmp < 0) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public List<String> getArgFieldNames() {
        return Arrays.asList(fieldName);
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, fieldName);
    }
}
