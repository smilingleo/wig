package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.WithFieldArg;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.model.HierarchicalMap;
import me.smilingleo.utils.FieldUtils;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.utils.Asserts;
import me.smilingleo.utils.Constants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class GroupBy implements Decorator, WithFieldArg {

    private static final String NAME = "GroupBy";
    private List<MergeField> byFields;

    private GroupBy(List<MergeField> byFields) {
        this.byFields = byFields;
    }

    public static GroupBy parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        Asserts.assertTrue(!arguments.isEmpty(), ErrorCode.InvalidFunctionArgument,
                format("Function %s requires at least one argument.", NAME));
        Asserts.assertTrue(arguments.size() <= 3, ErrorCode.InvalidFunctionArgument,
                format("Function %s can take no more than %d arguments but it received %d. %s", NAME, 3,
                        arguments.size(), label));
        List<MergeField> fields = arguments.stream()
                .map(str -> MergeFieldParser.parse(str))
                .collect(Collectors.toList());
        return new GroupBy(fields);
    }

    @Override
    public List<Map<String, Object>> evaluate(Object input) {
        Asserts.assertNotNull(input, format("Input argument for %s is null.", NAME));
        Asserts.assertType(input, List.class, format("Function %s expects a List type input, but it receives a %s.", NAME,
                input.getClass().getSimpleName()));
        List<Map<String, Object>> list = (List) input;
        int layer = 0;
        for (MergeField byField : byFields) {
            list = groupBy(list, byField, layer++);
        }
        return list;
    }

    /**
     * <p>for the first round of grouping, the list is the original list of map, we just need to group it normally.</p>
     *
     * <p>for the second layer, the input list is in format of {@code List<Map<String, List<Map<String, Object>>>}
     * we need to iterate the list and group the {@code Map<String, Object>} </p>
     *
     * <p>for the third layer, the input list is in format of
     * {@code List<Map<String, List<Map<String, List<Map<String, Object>>>>>}
     * </p>
     */
    private List<Map<String, Object>> groupBy(List<Map<String, Object>> list, MergeField byField, int layer) {
        if (layer == 0) {
            return group(list, byField);
        } else if (layer == 1) {
            for (Map<String, Object> item : list) {
                List<Map<String, Object>> list2 = (List<Map<String, Object>>) item.get(Constants.DERIVED_LIST_KEY);
                List<Map<String, Object>> grouped = group(list2, byField);
                item.put(Constants.DERIVED_LIST_KEY, grouped);
            }
            return list;
        } else if (layer == 2) {
            for (Map<String, Object> item : list) {
                List<Map<String, Object>> list2 = (List<Map<String, Object>>) item.get(Constants.DERIVED_LIST_KEY);
                for (Map<String, Object> item2 : list2) {
                    List<Map<String, Object>> list3 = (List<Map<String, Object>>) item2.get(Constants.DERIVED_LIST_KEY);
                    List<Map<String, Object>> grouped2 = group(list3, byField);
                    item2.put(Constants.DERIVED_LIST_KEY, grouped2);
                }
            }
            return list;
        } else {
            throw new ValidationException(
                    ErrorCode.InvalidFunctionArgument, format("Function %s only supports 3 layers of groups.", NAME));
        }
    }

    private List<Map<String, Object>> group(List<Map<String, Object>> list, MergeField byField) {
        Map<Object, List<Map<String, Object>>> grouped = list.stream()
                .filter(Objects::nonNull)
                // No matter if the byField has decorator or not,
                // we need to use field name without decorator to access data
                .collect(Collectors.groupingBy(item -> {
                    String mergeFieldArgument = byField.getInputFieldName();
                    Object byDottedPath = MapUtils.getByDottedPath(item, mergeFieldArgument);
                    if (byDottedPath == null) {
                        return "";
                    }
                    return byField.dataBind(byDottedPath).orElse(byDottedPath);
                }, LinkedHashMap::new, Collectors.toList()));
        List<Map<String, Object>> transformed = grouped.entrySet().stream()
                .map(entry -> {
                    HierarchicalMap parent = getParent(entry.getValue());
                    Map<String, Object> record = new HierarchicalMap(parent);
                    final AtomicReference<Object> keyValue = new AtomicReference(entry.getKey());
                    byField.getDecorators()
                            .ifPresent(decorators -> decorators.forEach(
                                    decorator -> keyValue.set(decorator.evaluate(keyValue.get()))));
                    // in case the key is dotted path
                    MapUtils.setByDottedPath(record, byField.toString(), keyValue.get());
                    record.put(Constants.DERIVED_LIST_KEY, entry.getValue());
                    return record;
                })
                .collect(Collectors.toList());
        return transformed;
    }


    @Override
    public List<String> getArgFieldNames() {
        return byFields.stream()
                .map(MergeField::getInputFieldName)
                .map(fieldName -> FieldUtils.getOriginalInputFieldName(fieldName).orElse(fieldName))
                .filter(StringUtils::notNullOrBlank)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME,
                byFields.stream().map(MergeField::toString).collect(Collectors.joining(",")));
    }

    private HierarchicalMap getParent(List<Map<String, Object>> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        Map<String, Object> head = values.get(0);
        return head instanceof HierarchicalMap ? ((HierarchicalMap) head).getParent() : null;
    }
}
