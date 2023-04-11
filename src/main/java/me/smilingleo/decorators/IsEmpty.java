package me.smilingleo.decorators;

import static me.smilingleo.utils.Asserts.assertType;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;

public final class IsEmpty implements Decorator, ReturningScalar {

    public static IsEmpty parse(String label) {
        if ("IsEmpty".equals(label)) {
            return new IsEmpty();
        }
        throw new ValidationException(ErrorCode.InvalidFunctionArgument, "IsEmpty decorator takes no argument.");
    }

    @Override
    public Boolean evaluate(Object input) {
        if (input == null) {
            return true;
        }
        assertType(input, List.class, "IsEmpty decorator expects a List type input, but it receives a "
                + input.getClass().getSimpleName());
        List<Object> list = new ArrayList<>((List) input);
        return list.isEmpty();
    }

    @Override
    public String toString() {
        return "IsEmpty";
    }
}
