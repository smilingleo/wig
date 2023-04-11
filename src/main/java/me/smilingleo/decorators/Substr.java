package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import me.smilingleo.utils.Asserts;
import me.smilingleo.utils.StringUtils;

import java.util.List;

public final class Substr implements Decorator, ReturningScalar {

    private static final String NAME = "Substr";

    private int begin;
    private int end;

    private Substr(int begin, int end) {
        this.begin = begin;
        this.end = end;
        Asserts.assertTrue(begin < end, ErrorCode.InvalidFunctionArgument,
                format("Invalid arguments for function %s, begin must be less than end index.", NAME));
        Asserts.assertTrue(begin >= 0 && end > 0, ErrorCode.InvalidFunctionArgument,
                format("Invalid arguments for function %s, begin and end must be positive.", NAME));
    }

    public static Substr parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        Asserts.assertTrue(arguments.size() == 2, ErrorCode.InvalidFunctionArgument,
                format("Function %s expects two arguments but it received %d. %s", NAME, arguments.size(), label));

        String beginStr = arguments.get(0);
        String endStr = arguments.get(1);
        try {
            return new Substr(Integer.parseInt(beginStr), Integer.parseInt(endStr));
        } catch (NumberFormatException nfe) {
            throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                    format("Function %s requires integer arguments. %s", NAME, label), nfe);
        }
    }

    @Override
    public Object evaluate(Object input) {
        if (input == null) {
            return null;
        }
        Asserts.assertType(input, String.class, NAME + " can only be used for text merge field.");
        String content = (String) input;
        if (begin >= content.length()) {
            return "";
        }
        if (content.length() < end) {
            return content.substring(begin);
        } else {
            return content.substring(begin, end);
        }
    }

    @Override
    public String toString() {
        return format("%s(%d,%d)", NAME, begin, end);
    }
}
