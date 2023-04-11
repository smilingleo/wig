package me.smilingleo.decorators;

import static me.smilingleo.utils.Asserts.assertType;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Return the size of a list input.</p>
 */
public final class Size implements Decorator, ReturningScalar {

    private Size() {
    }

    public static Size parse(String label) {
        if ("Size".equals(label)) {
            return new Size();
        }
        throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                "Invalid argument for function Size:" + label);
    }

    @Override
    public Integer evaluate(Object input) {
        if (input == null) {
            return 0;
        }
        assertType(input, List.class, "Function Size expects a List type input, but it receives a "
                + input.getClass().getSimpleName());
        List<Object> list = new ArrayList<>((List) input);
        return list.size();
    }

    @Override
    public String toString() {
        return "Size";
    }
}
