package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static java.lang.String.format;

import me.smilingleo.utils.StringUtils;

import java.util.List;

/**
 * <p>A decorator to provide default value in case the input is null or blank.</p>
 */
public final class Default implements Decorator, ReturningScalar {

    private static final String NAME = "Default";
    private String value;

    private Default(String value) {
        this.value = value;
    }

    public static Default parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() == 1, InvalidFunctionArgument,
                format("Function %s takes one argument.", NAME));
        return new Default(arguments.get(0));
    }

    @Override
    public Object evaluate(Object input) {
        if (input == null || input.toString().trim().length() == 0) {
            return StringUtils.unquote(value);
        } else {
            return input;
        }
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, value);
    }
}
