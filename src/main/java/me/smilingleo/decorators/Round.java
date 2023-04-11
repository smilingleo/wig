package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.StringUtils.parseFunctionArguments;
import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.utils.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public final class Round implements Decorator, ReturningScalar {

    private static final String NAME = "Round";

    private int precision;
    private Optional<RoundingMode> roundingMode = Optional.empty();

    private Round(int precision, Optional<RoundingMode> roundingMode) {
        this.precision = precision;
        this.roundingMode = roundingMode;
        assertTrue(precision >= 0 && precision <= 10, ErrorCode.InvalidFunctionArgument,
                format("Precision argument of %s should be less or equal to 10 and greater or equal to 0, but you gave %d",
                        NAME, precision));
    }

    public static Round parse(String label) {
        List<String> arguments = parseFunctionArguments(label);
        assertTrue(!arguments.isEmpty() && arguments.size() <= 2, InvalidFunctionArgument,
                format("Function %s expects one or two arguments.", NAME));
        String precisionStr = arguments.get(0);
        Optional<RoundingMode> roundingMode = arguments.size() == 1 ? Optional.empty()
                : Optional.of(RoundingMode.valueOf(arguments.get(1)));
        return new Round(Integer.parseInt(precisionStr), roundingMode);
    }

    @Override
    public Object evaluate(Object input) {
        if (input == null || String.valueOf(input).trim().isEmpty()) {
            return null;
        }
        String content = input.toString();
        assertTrue(StringUtils.isNumber(content), InvalidFunctionArgument,
                format("Function %s can only be used to a numeric input field.", NAME));
        BigDecimal decimal = new BigDecimal(content);
        BigDecimal result = decimal.setScale(precision, roundingMode.orElse(RoundingMode.HALF_UP));
        return result.toString();
    }

    @Override
    public String toString() {
        return format("%s(%d%s)", NAME, precision, roundingMode.map(mode -> "," + mode.name()).orElse(""));
    }
}
