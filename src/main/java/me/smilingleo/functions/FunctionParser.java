package me.smilingleo.functions;

import static me.smilingleo.utils.Constants.FUNC_PREFIX;
import static me.smilingleo.utils.ReflectionUtils.invokeParse;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FunctionParser {

    private static final List<Class<? extends Function>> ALL_FUNCTION_CLASSES = new LinkedList<>();

    static {
        ALL_FUNCTION_CLASSES.add(Calc.class);
        ALL_FUNCTION_CLASSES.add(Today.class);
    }

    public static Function parse(String label) {
        for (Class<? extends Function> cmdClass : ALL_FUNCTION_CLASSES) {
            String fnName = FUNC_PREFIX + cmdClass.getSimpleName();
            if (Objects.equals(label, fnName) || label.startsWith(fnName + "(")) {
                Function fn = invokeParse(cmdClass, label);
                return fn;
            }
        }
        throw new ValidationException(ErrorCode.UnknownFunction, "Unknown function: " + label);
    }
}
