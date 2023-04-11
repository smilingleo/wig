package me.smilingleo.commands;

import static me.smilingleo.utils.ReflectionUtils.invokeParse;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import me.smilingleo.utils.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CommandParser {

    private static final List<Class<? extends Command>> ALL_CMD_CLASSES = new LinkedList<>();

    static {
        ALL_CMD_CLASSES.add(Assign.class);
        ALL_CMD_CLASSES.add(Column.class);
        ALL_CMD_CLASSES.add(Compose.class);
        ALL_CMD_CLASSES.add(ListToDict.class);
    }

    public static Command parse(String label) {
        for (Class<? extends Command> cmdClass : ALL_CMD_CLASSES) {
            String cmdName = Constants.CMD_PREFIX + cmdClass.getSimpleName();
            if (Objects.equals(label, cmdName) || label.startsWith(cmdName + "(")) {
                Command cmd = invokeParse(cmdClass, label);
                return cmd;
            }
        }
        throw new ValidationException(ErrorCode.UnknownCommand, "Unknown command: " + label);
    }
}
