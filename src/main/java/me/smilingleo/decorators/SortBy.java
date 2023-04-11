package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.WithFieldArg;
import me.smilingleo.utils.FieldUtils;
import me.smilingleo.utils.MapUtils;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.utils.Asserts;
import me.smilingleo.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SortBy implements Decorator, WithFieldArg {

    private static final String NAME = "SortBy";
    private List<SortByArgument> byArguments;

    private SortBy(List<SortByArgument> byArguments) {
        this.byArguments = byArguments;
    }

    public static SortBy parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        Asserts.assertTrue(!arguments.isEmpty(), ErrorCode.InvalidFunctionArgument,
                format("Function %s expects at least two arguments.", NAME));
        Asserts.assertTrue(arguments.size() % 2 == 0, ErrorCode.InvalidFunctionArgument,
                format("Invalid argument for %s, they must be pair of <field,direction>", NAME));

        List<SortByArgument> byArguments = new LinkedList<>();
        for (int i = 0; i < arguments.size(); i = i + 2) {
            String mergeFieldStr = arguments.get(i);
            String direction = arguments.get(i + 1);
            SortByArgument argument = new SortByArgument(MergeFieldParser.parse(mergeFieldStr),
                    SortOrder.valueOf(direction));
            byArguments.add(argument);
        }
        Asserts.assertTrue(!byArguments.isEmpty() && byArguments.size() <= 3, ErrorCode.InvalidFunctionArgument,
                format("Function %s can only take up to 3 pairs of arguments.", NAME));
        return new SortBy(byArguments);
    }

    @Override
    public List<java.util.Map<String, Object>> evaluate(Object input) {
        if (input == null) {
            return Collections.emptyList();
        }
        Asserts.assertType(input, List.class, format("Function %s expects a List type input, but it receives a %s.", NAME,
                input.getClass().getSimpleName()));
        List<java.util.Map<String, Object>> list = new ArrayList<>((List) input);

        Comparator<java.util.Map<String, Object>> sortByComparator = null;
        for (SortByArgument byArg : byArguments) {
            Comparator<Map<String, Object>> comparator = (o1, o2) -> {
                Object value1 = MapUtils.getByDottedPath(o1, byArg.getField().toString());
                Object value2 = MapUtils.getByDottedPath(o2, byArg.getField().toString());

                if (byArg.getSortOrder().equals(SortOrder.ASC)) {
                    return Operator.compareTo(value1, value2);
                } else {
                    return Operator.compareTo(value2, value1);
                }
            };
            if (sortByComparator == null) {
                sortByComparator = comparator;
            } else {
                sortByComparator = sortByComparator.thenComparing(comparator);
            }
        }

        list.sort(sortByComparator);
        return list;
    }

    @Override
    public List<String> getArgFieldNames() {
        return byArguments.stream()
                .map(sortByArgument -> {
                    String fieldName = sortByArgument.getField().getInputFieldName();
                    return FieldUtils.getOriginalInputFieldName(fieldName).orElse(fieldName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String argStr = byArguments.stream()
                .map(byArg -> byArg.getField() + "," + byArg.getSortOrder())
                .collect(Collectors.joining(","));
        return format("%s(%s)", NAME, argStr);
    }
}
