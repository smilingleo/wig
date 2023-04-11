package me.smilingleo.decorators;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import me.smilingleo.utils.Asserts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Uniq implements Decorator {

    private Uniq() {
    }

    public static Uniq parse(String label) {
        if (Objects.equals("Uniq", label)) {
            return new Uniq();
        }
        if (label.startsWith("Uniq(")) {
            throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                    "Uniq decorator takes no argument.");
        }
        throw new ValidationException(ErrorCode.InvalidFunctionArgument, "Invalid name for Uniq decorator.");
    }

    @Override
    public List<java.util.Map<String, Object>> evaluate(Object input) {
        if (input == null) {
            return Collections.emptyList();
        }
        Asserts.assertType(input, List.class, "Uniq decorator expects a List type input, but it receives a "
                + input.getClass().getSimpleName());
        if (((List) input).isEmpty()) {
            return Collections.emptyList();
        }
        Object headObj = ((List) input).get(0);
        Asserts.assertType(headObj, java.util.Map.class,
                "Uniq decorator expects an object List type input, but it receives a list of "
                        + headObj.getClass().getSimpleName());
        List<java.util.Map<String, Object>> list = new ArrayList<>((List) input);
        return list.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Uniq";
    }
}
