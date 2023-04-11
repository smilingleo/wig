package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static java.lang.String.format;

import me.smilingleo.WithFieldArg;
import me.smilingleo.utils.FieldUtils;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import me.smilingleo.utils.Asserts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FilterByValue implements Decorator, WithFieldArg {

    private static final String NAME = "FilterByValue";
    private MergeField field;
    private Operator operator;
    private String value;

    private FilterByValue(MergeField field, Operator operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public MergeField getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getValue() {
        String str = StringUtils.unquote(value);
        return StringUtils.urlDecode(str);
    }

    public static FilterByValue parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);

        Asserts.assertTrue(arguments.size() >= 2, InvalidFunctionArgument,
                format("Function %s requires at least %d arguments but it only received %d. %s", NAME, 2, arguments.size(),
                        label));

        String content = arguments.get(0);
        String operator = arguments.get(1);

        Operator op = Operator.valueOf(operator);

        String value = null;
        if (Operator.IS_NULL != op && Operator.NOT_NULL != op) {
            Asserts.assertTrue(arguments.size() == 3, InvalidFunctionArgument,
                    format("Value argument is required but not specified for function %s: %s", NAME, label));
            value = arguments.get(2);
        }

        MergeField field = MergeFieldParser.parse(content);
        return new FilterByValue(field, op, value);
    }

    @Override
    public List<Map<String, Object>> evaluate(Object input) {
        if (input == null) {
            return Collections.emptyList();
        }
        Asserts.assertType(input, List.class,
                format("Function %s expects a List type input, but it receives a %s.", NAME,
                        input.getClass().getSimpleName()));
        List<Map<String, Object>> list = new ArrayList<>((List) input);
        return list.stream()
                .filter(Objects::nonNull)
                .filter(record -> {
                    String fieldName = field.getInputFieldName();
                    Object valueByFieldName = MapUtils.getByDottedPath(record, fieldName);
                    Object leftSide = field.dataBind(valueByFieldName).orElse(valueByFieldName);
                    return operator.compare(leftSide, getValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getArgFieldNames() {
        String fieldName = field.getInputFieldName();
        return Arrays.asList(FieldUtils.getOriginalInputFieldName(fieldName).orElse(fieldName));
    }

    @Override
    public String toString() {
        return operator == Operator.IS_NULL || operator == Operator.NOT_NULL ? format("%s(%s,%s)", NAME, field, operator.name())
                : format("%s(%s,%s,%s)", NAME, field, operator.name(), value);
    }
}
