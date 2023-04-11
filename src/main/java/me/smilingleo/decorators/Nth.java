package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.Asserts.assertType;
import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class Nth implements Decorator {

    private static final String NAME = "Nth";
    private int index;

    private Nth(int index) {
        assertTrue(index != 0, ErrorCode.InvalidFunctionArgument,
                NAME + " function is 1-based indexing.");
        this.index = index;
    }

    public static Nth parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() == 1, InvalidFunctionArgument,
                format("Function %s requires %d argument but it only received %d.", NAME, 1, arguments.size()));

        try {
            return new Nth(Integer.parseInt(arguments.get(0)));
        } catch (NumberFormatException nfe) {
            throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                    format("Function %s requires an integer argument.", NAME), nfe);
        }
    }

    @Override
    public Object evaluate(Object input) {
        if (input == null) {
            return null;
        }
        assertType(input, List.class, format("Function %s expects a List type input, but it receives a %s", NAME,
                input.getClass().getSimpleName()));
        List<Object> list = new ArrayList<>((List) input);
        int size = list.size();

        if (Math.abs(index) > list.size()) {
            return null;
        }
        // give a 3-length list, Nth(-2) returns list.get(1), Nth(2) returns, list.get(1).
        return index > 0 ? list.get(index - 1) : list.get(size + index);
    }

    @Override
    public String toString() {
        return format("%s(%d)", NAME, index);
    }
}
