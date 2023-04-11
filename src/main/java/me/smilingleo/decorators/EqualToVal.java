package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.utils.StringUtils;

import me.smilingleo.utils.Asserts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * <p>Compare the input with a value.</p>
 */
public final class EqualToVal implements Decorator, ReturningScalar {

    private static final String NAME = "EqualToVal";
    private String value;

    private EqualToVal(String varName) {
        this.value = varName;
    }

    public static EqualToVal parse(String label) {
        List<String> list = StringUtils.parseFunctionArguments(label);
        Asserts.assertTrue(list.size() == 1, ErrorCode.InvalidFunctionArgument,
                format("Invalid argument for function %s: %s", NAME, label));
        return new EqualToVal((list.get(0)));
    }

    /**
     * @param input should be a scalar type.
     */
    @Override
    public Boolean evaluate(Object input) {
        if (input == null) {
            return false;
        }
        if (input instanceof Number || StringUtils.isNumber(input.toString())) {
            return new BigDecimal(input.toString()).compareTo(new BigDecimal(value)) == 0;
        } else if (input instanceof Boolean) {
            return Boolean.parseBoolean(input.toString()) == Boolean.parseBoolean(value);
        } else {
            return Objects.equals(input, value);
        }
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, value);
    }
}
