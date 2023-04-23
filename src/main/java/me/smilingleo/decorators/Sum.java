package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.Asserts.assertType;
import static me.smilingleo.utils.StringUtils.parseFunctionArguments;
import static java.lang.String.format;

import me.smilingleo.WithFieldArg;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.FieldUtils;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Sum implements Decorator, WithFieldArg, ReturningScalar {

    private static final String NAME = "Sum";
    private MergeField field;

    private Sum(MergeField field) {
        this.field = field;
    }

    public static Sum parse(String label) {
        List<String> arguments = parseFunctionArguments(label);
        assertTrue(arguments.size() == 1, InvalidFunctionArgument,
                format("Function %s expects one argument but it received %d. %s", NAME, arguments.size(), label));

        String content = arguments.get(0);
        MergeField field = MergeFieldParser.parse(content);
        return new Sum(field);
    }

    @Override
    public Double evaluate(Object input) {
        if (input == null) {
            return Double.valueOf(0);
        }
        assertType(input, List.class, format("Function %s expects a List type input, but it receives a %s.", NAME,
                input.getClass().getSimpleName()));
        if (((List) input).isEmpty()) {
            return Double.valueOf(0);
        }
        Object headObj = ((List) input).get(0);
        assertType(headObj, java.util.Map.class,
                format("Function %s expects an object List type input, but it receives a list of %s", NAME,
                        headObj.getClass().getSimpleName()));
        List<Map<String, Object>> list = new ArrayList<>((List) input);

        return list.stream()
                .reduce(Double.valueOf(0), (acc, map) -> {
                    Object value = MapUtils.getByDottedPath(map, field.getInputFieldName());
                    if (value == null) {
                        return acc;
                    }
                    if (value instanceof String && StringUtils.isNumber(value.toString())) {
                        value = Double.parseDouble(value.toString());
                    }

                    if (!(value instanceof Number)) {
                        throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                                format("Function %s can only take a numeric field, but %s type is %s",
                                        NAME, field.getInputFieldName(), value.getClass().getSimpleName()));
                    }
                    return ((Number) value).doubleValue() + acc;
                }, (d1, d2) -> d1 + d2);
    }

    @Override
    public List<String> getArgFieldNames() {
        String fieldName = field.getInputFieldName();
        return Arrays.asList(FieldUtils.getOriginalInputFieldName(fieldName).orElse(fieldName));
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, field);
    }
}
