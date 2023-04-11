package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.Asserts.assertType;
import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Last implements Decorator {

    private static final String NAME = "Last";
    private int count;

    private Last(int count) {
        assertTrue(count > 0, ErrorCode.InvalidFunctionArgument,
                format("Only positive number is allowed as argument of function %s.", NAME));
        this.count = count;
    }

    public static Last parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() == 1, InvalidFunctionArgument,
                format("Function %s requires %d argument but it only received %d.", NAME, 1, arguments.size()));
        try {
            return new Last(Integer.parseInt(arguments.get(0)));
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
        assertType(input, List.class, format("Function %s expects a List type input, but it receives a %s.", NAME,
                input.getClass().getSimpleName()));
        List<Object> list = new ArrayList<>((List) input);
        long skip = 0;
        if (list.size() > count) {
            skip = list.size() - count;
        }
        return list.stream()
                .skip(skip)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return format("%s(%d)", NAME, count);
    }
}
