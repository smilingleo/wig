package me.smilingleo.commands;

import static me.smilingleo.exceptions.ErrorCode.InvalidCommandArgument;
import static me.smilingleo.exceptions.ErrorCode.InvalidValue;
import static me.smilingleo.utils.Asserts.assertTrue;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import me.smilingleo.ChildrenAware;
import me.smilingleo.CreateNewObject;
import me.smilingleo.RenderContext;
import me.smilingleo.WithInputField;
import me.smilingleo.decorators.MergeField;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.model.HierarchicalMap;
import me.smilingleo.utils.Asserts;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class Compose implements Command, ChildrenAware, CreateNewObject {

    private static final String NAME = "Cmd_Compose";

    private String newListName;
    private List<WithInputField> children = new LinkedList<>();

    private Compose(String newListName) {
        this.newListName = newListName;
        RenderContext.getContext().addNewObject(newListName);
    }

    public static Compose parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() == 1, InvalidCommandArgument, format("Command %s expected one argument.", NAME));
        String newListName = arguments.get(0);
        assertTrue(!newListName.contains("."), InvalidCommandArgument,
                format("The list name of %s cannot be dotted path.", label));
        return new Compose(newListName);
    }

    @Override
    public List<String> getArgFieldNames() {
        return asList(newListName);
    }

    /**
     * <p>By this input field name, we can't get any data from the dataset via json path,
     * in such a case, the entire data set should be passed in.</p>
     */
    @Override
    public String getInputFieldName() {
        return "";
    }

    /**
     * @param input <p>A map contains all being-composed list input is expected here.</p>
     */
    @Override
    public void execute(Object input) {
        if (input == null) {
            return;
        }
        Asserts.assertType(input, Map.class,
                format("Command %s expected a object input but got a %s", NAME, input.getClass().getSimpleName()));
        Map<String, Object> typedInput = (Map<String, Object>) input;

        List<Column> columns = children.stream()
                .filter(child -> child instanceof Column)
                .map(child -> (Column) child)
                .collect(toList());

        List newList = new LinkedList();
        for (WithInputField listField : children) {
            if (!(listField instanceof MergeField)) {
                continue;
            }
            MergeField listMergeField = (MergeField) listField;
            String inputFieldName = listField.getInputFieldName();
            Object list = MapUtils.getByDottedPath(typedInput, inputFieldName);
            listMergeField.dataBind(list).ifPresent(output -> {
                if (output instanceof List) {
                    // construct a new map according to Cmd_column
                    List outputList = (List) output;

                    for (Object item : outputList) {
                        Asserts.assertType(item, List.class,
                                format("Command %s expected a List type but got a %s.", NAME,
                                        item.getClass().getSimpleName()));
                        List itemList = (List) item;
                        if (itemList.size() != columns.size()) {
                            throw new ValidationException(ErrorCode.InvalidCommandArgument,
                                    "Columns and data do not match");
                        }

                        HierarchicalMap record = new HierarchicalMap(typedInput instanceof HierarchicalMap
                                ? (HierarchicalMap) typedInput : null);
                        for (int i = 0; i < columns.size(); i++) {
                            record.put(columns.get(i).getColumnName(), itemList.get(i));
                        }
                        newList.add(record);
                    }
                } else {
                    throw new ValidationException(InvalidValue, "Unexpected output");
                }
            });
        }
        typedInput.put(newListName, newList);
    }

    @Override
    public void addChild(WithInputField inputField) {
        children.add(inputField);
        if (inputField instanceof Column) {
            RenderContext.getContext().registerNewField(newListName, ((Column)inputField).getColumnName());
        }
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, newListName);
    }

    @Override
    public String newObjectName() {
        return newListName;
    }
}
