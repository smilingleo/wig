package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.exceptions.ErrorCode.InvalidValue;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.StringUtils.isText;
import static me.smilingleo.utils.StringUtils.unquote;
import static me.smilingleo.utils.StringUtils.urlDecode;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import me.smilingleo.WithFieldArg;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.Asserts;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Map implements Decorator, WithFieldArg {

    private static final String NAME = "Map";
    private List<MergeField> fields;

    private Map(List<MergeField> fields) {
        this.fields = fields;
    }

    public static Map parse(String label) {
        assertTrue(!label.contains("|"), InvalidFunctionArgument,
                format("Argument of %s function can't be decorated: %s", NAME, label));
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(!arguments.isEmpty(), InvalidFunctionArgument,
                format("Function %s requires at least one argument.", NAME));
        assertTrue(arguments.get(0).indexOf('.') < 0, ErrorCode.InvalidFunctionArgument,
                format("Function %s can't accept a dotted path as first argument.", NAME));
        return new Map(arguments.stream().map(content -> MergeFieldParser.parse(content)).collect(toList()));
    }

    @Override
    public List evaluate(Object input) {
        if (input == null) {
            return Collections.emptyList();
        }
        Asserts.assertType(input, List.class,
                format("Function %s expects a List type input, but it receives a %s", NAME,
                        input.getClass().getSimpleName()));
        if (((List) input).isEmpty()) {
            return Collections.emptyList();
        }
        Object headObj = ((List) input).get(0);
        Asserts.assertType(headObj, java.util.Map.class,
                format("Function %s expects an object List type input, but it receives a list of %s", NAME,
                        headObj.getClass().getSimpleName()));

        List<java.util.Map<String, Object>> list = new ArrayList<>((List) input);

        return list.stream()
                .map(record -> {
                    List row = new LinkedList();
                    for (MergeField field : fields) {
                        String fieldName = field.getInputFieldName();
                        Object value = isText(field.toString()) ? urlDecode(unquote(fieldName))
                                : MapUtils.getByDottedPath(record, fieldName);
                        if (value == null) {
                            value = ""; // row needs to be with fixed columns
                        }
                        if (value instanceof List) {
                            throw new ValidationException(InvalidValue,
                                    format("Function %s can not be used to fetch a list type, use %s instead.", NAME,
                                            FlatMap.NAME));
                        }
                        row.add(value);
                    }
                    // If it's a multi-field map, we will return a list, otherwise, an object.
                    return fields.size() == 1 ? row.get(0) : row;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getArgFieldNames() {
        return fields.stream()
                .filter(field -> !isText(field.toString()))
                .map(field -> field.toString())
                .collect(toList());
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, fields.stream().map(Object::toString).collect(Collectors.joining(",")));
    }
}
