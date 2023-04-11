package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.Asserts.assertType;
import static java.lang.String.format;

import me.smilingleo.RenderContext;
import me.smilingleo.WithFieldArg;
import me.smilingleo.model.HierarchicalMap;
import me.smilingleo.utils.FieldUtils;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FilterByRef implements Decorator, WithFieldArg {

    private static final String NAME = "FilterByRef";
    private MergeField field;
    private Operator operator;
    private String referenceName;

    private FilterByRef(MergeField field, Operator operator, String referenceName) {
        this.field = field;
        this.operator = operator;
        this.referenceName = referenceName;
    }

    public MergeField getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public static FilterByRef parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() == 3, InvalidFunctionArgument,
                format("Function %s requires %d arguments but it only received %d.", NAME, 3, arguments.size()));
        String content = arguments.get(0);
        String operator = arguments.get(1);
        String referenceName = arguments.get(2);

        MergeField field = MergeFieldParser.parse(content);
        return new FilterByRef(field, Operator.valueOf(operator), referenceName);
    }

    @Override
    public List<Map<String, Object>> evaluate(Object input) {
        if (input == null) {
            return Collections.emptyList();
        }
        assertType(input, List.class,
                format("Function %s expects a List type input, but it receives a %s", NAME,
                        input.getClass().getSimpleName()));
        List<Map<String, Object>> list = new ArrayList<>((List) input);
        return list.stream()
                .filter(Objects::nonNull)
                .filter(record -> {
                    // we expect the record is HierarchicalMap
                    String fieldName = field.getInputFieldName();
                    Object valueByFieldName = MapUtils.getByDottedPath(record, fieldName);
                    Object leftSide = field.dataBind(valueByFieldName).orElse(valueByFieldName);
                    if (leftSide == null) {
                        return false;
                    }
                    Object rightSide = getReferenceValue(record, referenceName);
                    if (rightSide == null) {
                        return false;
                    }

                    return operator.compare(leftSide, rightSide);
                })
                .collect(Collectors.toList());
    }

    private Object getReferenceValue(Map<String, Object> record, String referenceName) {
        Object referenceValue = HierarchicalMap.fromMap(record).findValue(referenceName);
        if (Objects.nonNull(referenceValue)) {
            return referenceValue;
        } else if (RenderContext.getContext().isVariable(referenceName)) {
            return RenderContext.getContext().getVariable(referenceName).getEvaluatedValue();
        }
        return null;
    }

    @Override
    public List<String> getArgFieldNames() {
        String fieldName = field.getInputFieldName();
        return Arrays.asList(FieldUtils.getOriginalInputFieldName(fieldName).orElse(fieldName), MergeFieldParser.parse(referenceName).getInputFieldName());
    }

    @Override
    public String toString() {
        return format("%s(%s,%s,%s)", NAME, field, operator.name(), referenceName);
    }
}
