package me.smilingleo.decorators;

import static me.smilingleo.RenderContext.getContext;
import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.exceptions.ErrorCode.InvalidValue;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.Asserts.assertType;
import static me.smilingleo.utils.Constants.DERIVED_LIST_KEY;
import static java.lang.String.format;

import me.smilingleo.Variable;
import me.smilingleo.WithFieldArg;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FlatMap implements Decorator, WithFieldArg {

    static final String NAME = "FlatMap";
    private MergeField field;

    private FlatMap(MergeField field) {
        assertTrue(!field.getDecorators().isPresent(), InvalidFunctionArgument,
                format("Field argument of %s function can't be decorated.", NAME));
        this.field = field;
    }

    public static FlatMap parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() == 1, InvalidFunctionArgument, format("Function %s requires one argument.", NAME));
        String content = arguments.get(0);
        assertTrue(content.indexOf('.') < 0, InvalidFunctionArgument,
                format("Function %s can't accept a dotted path as first argument", NAME));

        MergeField field = MergeFieldParser.parse(content);
        return new FlatMap(field);
    }

    @Override
    public List<java.util.Map<String, Object>> evaluate(Object input) {
        if (input == null) {
            return Collections.emptyList();
        }
        assertType(input, List.class, format("Function %s expects a List type input, but it receives a ", NAME,
                input.getClass().getSimpleName()));
        if (((List) input).isEmpty()) {
            return Collections.emptyList();
        }
        Object headObj = ((List) input).get(0);
        assertType(headObj, java.util.Map.class,
                format("Function %s expects an object List type input, but it receives a list of ", NAME,
                        headObj.getClass().getSimpleName()));

        List<java.util.Map<String, Object>> list = new ArrayList<>((List) input);
        return list.stream()
                .flatMap(record -> {
                    String fieldName = field.getInputFieldName();
                    Object value = record.get(fieldName);
                    if (value == null) {
                        return Stream.empty();
                    }
                    if (!(value instanceof List)) {
                        throw new ValidationException(InvalidValue,
                                format("Function %s can only be used to fetch a nested list, not scalar or object type.",
                                        NAME));
                    }
                    return ((List<java.util.Map<String, Object>>) value).stream();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getArgFieldNames() {
        String inputFieldName = field.getInputFieldName();
        if (inputFieldName.equals(DERIVED_LIST_KEY)) {
            return Collections.emptyList();
        }

        Variable variable = getContext().getVariable(inputFieldName);
        if (variable != null) {
            if (!variable.isScalar()) {
                if (variable.getMergeField().getInputFieldName().equals(DERIVED_LIST_KEY)) {
                    return Collections.emptyList();
                } else {
                    return Arrays.asList(variable.getMergeField().getInputFieldName());
                }
            } else {
                return Collections.emptyList();
            }
        } else {
            return Arrays.asList(inputFieldName);
        }
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, field);
    }
}
