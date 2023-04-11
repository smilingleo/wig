package me.smilingleo.commands;

import static me.smilingleo.exceptions.ErrorCode.InvalidCommandArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.Asserts.assertType;
import static me.smilingleo.utils.StringUtils.notNullOrBlank;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import me.smilingleo.CreateNewObject;
import me.smilingleo.RenderContext;
import me.smilingleo.WithFieldArg;
import me.smilingleo.decorators.MergeField;
import me.smilingleo.decorators.MergeFieldParser;
import me.smilingleo.model.HierarchicalMap;
import me.smilingleo.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Transform a list of object into a dict object.</p>
 *
 * <p>Provided you have a custom object with columns, `default__messages(locale, key, message)`,
 * you want to access message by key, you can use this command like:</p>
 * <pre>
 *     {{Cmd_ListToDict(default__messages|Filter(locale,EQ,en_US),key,message,Message)}}
 * </pre>
 * <p>This command takes in 4 parameters:</p>
 * <ul>
 *     <li>A list of object</li>
 *     <li>key attribute name</li>
 *     <li>value column name</li>
 *     <li>New Dict name, so that you can reference this object in the template.</li>
 * </ul>
 * <p>For example, with above command, we can then do localization like:</p>
 * <pre>
 *     {{Message.invoice_title}}
 *     {{Message.account_name}}
 * </pre>
 * Whereas `invoice_title` and `account_name` are two values of the `key` column.
 *
 * <h4>Why not another decorator?</h4>
 * <p>It's because we need to insert an object to the context stack and refer to it later by name,
 * we can't do that with decorator.</p>
 */
public final class ListToDict implements Command, CreateNewObject {

    private static final String NAME = "Cmd_ListToDict";

    private MergeField listField;
    private String keyColumnName;
    private String valueColumnName;
    private String newDictName;

    private ListToDict(MergeField listField, String keyColumnName, String valueColumnName, String newDictName) {
        assertTrue(listField != null, InvalidCommandArgument,
                "list field argument can't be null or blank for command " + NAME);
        assertTrue(notNullOrBlank(keyColumnName), InvalidCommandArgument,
                "key column name argument can't be null or blank for command " + NAME);
        assertTrue(notNullOrBlank(valueColumnName), InvalidCommandArgument,
                "value column name argument can't be null or blank for command " + NAME);
        assertTrue(notNullOrBlank(newDictName), InvalidCommandArgument,
                "new dict name argument can't be null or blank for command " + NAME);
        assertTrue(newDictName.indexOf('.') < 0, InvalidCommandArgument,
                format("Dotted path can not be used as a name of new dict object for %s command, '%s' is invalid.",
                        NAME, newDictName));

        this.listField = listField;
        this.keyColumnName = keyColumnName;
        this.valueColumnName = valueColumnName;
        this.newDictName = newDictName;
        RenderContext context = RenderContext.getContext();
        context.addNewObject(newDictName);
        // the fields are from custom object data, so we can't know at compile time
        // just allow any field name.
        context.registerNewField(newDictName, "*");
    }

    public static ListToDict parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() == 4, InvalidCommandArgument,
                format("Command %s expected 4 arguments, but it received %d. %s", NAME, arguments.size(), label));
        String listFieldName = arguments.get(0);
        String keyColumn = arguments.get(1);
        String valueColumn = arguments.get(2);
        String newDictName = arguments.get(3);
        MergeField field = MergeFieldParser.parse(listFieldName);
        return new ListToDict(field, keyColumn, valueColumn, newDictName);
    }

    /**
     * @param input the parent of list field to be transformed. Usually, the root.
     */
    @Override
    public void execute(Object input) {
        if (input == null) {
            return;
        }
        assertType(input, Map.class, format("Command %s expects an object type input, but it received a %s.", NAME,
                input.getClass().getSimpleName()));

        HierarchicalMap map = HierarchicalMap.fromMap((Map<String, Object>) input);

        Object listObj = map.findValue(listField.getInputFieldName());

        if (listObj != null) {
            Optional<Object> bindOpt = listField.dataBind(listObj);
            if (bindOpt.isPresent()) {
                Object data = bindOpt.get();
                assertTrue(data instanceof List, InvalidCommandArgument,
                        format("Command %s expects a list for its first argument, but it got a %s.",
                                NAME, data.getClass().getSimpleName()));
                List<Map<String, Object>> list = (List<Map<String, Object>>) data;
                HierarchicalMap hierarchicalMap = new HierarchicalMap();
                // transform a list into a dict.
                for (Map<String, Object> record : list) {
                    hierarchicalMap.put(record.get(keyColumnName).toString(), record.get(valueColumnName));
                }
                // add a new attribute under current object context.
                map.put(newDictName, hierarchicalMap);
            }
        }
    }

    @Override
    public List<String> getArgFieldNames() {
        // since the key/value columns are attribute of list field object.
        List<String> fields = listField.getDecorators()
                .map(decorators -> decorators.stream()
                        .filter(decorator -> decorator instanceof WithFieldArg)
                        .flatMap(decorator -> ((WithFieldArg) decorator).getArgFieldNames().stream())
                        .collect(toList())
                )
                .orElse(new LinkedList<String>());
        fields.add(keyColumnName);
        fields.add(valueColumnName);
        return fields;
    }

    @Override
    public String getInputFieldName() {
        return listField.getInputFieldName();
    }

    @Override
    public MergeField getInputField() {
        return listField;
    }

    @Override
    public String toString() {
        return format("%s(%s,%s,%s,%s)", NAME, listField, keyColumnName, valueColumnName, newDictName);
    }

    @Override
    public String newObjectName() {
        return newDictName;
    }
}
