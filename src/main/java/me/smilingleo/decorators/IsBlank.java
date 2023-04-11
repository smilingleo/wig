package me.smilingleo.decorators;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.Asserts;

public final class IsBlank implements Decorator, ReturningScalar {

    public static IsBlank parse(String label) {
        if ("IsBlank".equals(label)) {
            return new IsBlank();
        }
        throw new ValidationException(ErrorCode.InvalidFunctionArgument, "IsBlank decorator takes no argument.");
    }

    @Override
    public Boolean evaluate(Object input) {
        if (input == null) {
            return true;
        }
        Asserts.assertType(input, String.class, "IsBlank decorator expects a text type input, but it receives a "
                + input.getClass().getSimpleName());
        String text = (String) input;
        return text.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "IsBlank";
    }
}
