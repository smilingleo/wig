package me.smilingleo.commands;

import static me.smilingleo.exceptions.ErrorCode.InnerTextRequired;
import static me.smilingleo.exceptions.ErrorCode.InvalidCommandArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static java.lang.String.format;
import static java.util.Arrays.asList;

import me.smilingleo.utils.StringUtils;

import java.util.List;

public final class Column implements Command {

    private static final String NAME = "Cmd_Column";

    private String columnName;
    private String label;

    private Column(String columnName, String label) {
        this.columnName = columnName;
        this.label = label;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getLabel() {
        return label;
    }

    /**
     * This command is special it takes a inner text, like `{{#Cmd_Column(Amount)}}Invoice
     * Amount{{/Cmd_Column(Amount)}}`
     */
    public static Column parse(String text) {
        String[] lines = text.split("\\n");
        assertTrue(lines.length == 2, InnerTextRequired, NAME + " command requires two lines of text to parse.");

        String label = lines[0].trim();
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() == 1, InvalidCommandArgument, format("Command %s expected one argument.", NAME));

        String columnName = arguments.get(0);
        return new Column(columnName, lines[1].trim());
    }

    @Override
    public List<String> getArgFieldNames() {
        return asList(columnName);
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
     * <p>See {@link Compose#execute(Object)}</p>
     */
    @Override
    public void execute(Object input) {
        // do nothing, the logic is in its parent, aka, Cmd_Compose.
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, columnName);
    }
}
