package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.StringUtils;

import me.smilingleo.utils.Asserts;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Skip implements Decorator {
    private static final String NAME = "Skip";
    private int count;

    private Skip(int count) {
        Asserts.assertTrue(count > 0, ErrorCode.InvalidFunctionArgument,
                format("Only positive number is allowed as argument of function %s.", NAME));
        this.count = count;
    }

    public static Skip parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        Asserts.assertTrue(arguments.size() == 1, InvalidFunctionArgument,
                format("Function %s requires %d argument but it only received %d.", NAME, 1, arguments.size()));
        try {
            return new Skip(Integer.parseInt(arguments.get(0)));
        } catch (NumberFormatException nfe) {
            throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                    format("Function %s requires an integer argument.", NAME), nfe);
        }
    }

    @Override
    public List<Object> evaluate(Object input) {
        if (input == null) {
            return Collections.emptyList();
        }
        Asserts.assertType(input, List.class, format("Function %s expects a List type input, but it receives a %s.", NAME,
                input.getClass().getSimpleName()));
        List<Object> list = (List) input;
        return list.stream()
                .skip(count)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return format("%s(%d)", NAME, count);
    }
}
