package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.Asserts.assertType;
import static me.smilingleo.utils.StringUtils.parseFunctionArguments;
import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class DateAdd implements Decorator, ReturningScalar {

    private static final String ISO_PATTERN = "yyyy-MM-dd";

    private static final String NAME = DateAdd.class.getSimpleName();

    enum Unit {
        D, M, Y
    }

    private int count;
    private Unit unit;

    private DateAdd(int n, String unitStr) {
        this.count = n;
        try {
            this.unit = Unit.valueOf(unitStr);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(InvalidFunctionArgument,
                    format("Invalid argument for function %s, the second argument should be one of '%s'", NAME,
                            String.join(",", Arrays.stream(Unit.values())
                                    .map(Object::toString)
                                    .collect(Collectors.toList()))), e);
        }

        assertTrue(n != 0, ErrorCode.InvalidFunctionArgument,
                format("Invalid arguments for function %s, the first argument cannot be zero.", NAME));
    }

    public static DateAdd parse(String label) {
        List<String> arguments = parseFunctionArguments(label);
        assertTrue(arguments.size() == 2, InvalidFunctionArgument,
                format("Function %s expects two arguments but it received %d. %s", NAME, arguments.size(), label));

        String countStr = arguments.get(0);
        String unitStr = arguments.get(1);
        try {
            return new DateAdd(Integer.parseInt(countStr), unitStr);
        } catch (NumberFormatException nfe) {
            throw new ValidationException(InvalidFunctionArgument,
                    format("Function %s requires integer arguments. %s", NAME, label), nfe);
        }
    }

    @Override
    public Object evaluate(Object input) {
        if (input == null) {
            return null;
        }
        assertType(input, String.class, NAME + " can only be used for date-typed merge field.");
        String content = (String) input;
        // no need to worry about the timezone since it's relative to the same timezone.
        DateFormat df = new SimpleDateFormat(ISO_PATTERN, Locale.US);
        try {
            Date date = df.parse(content);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            switch (unit) {
                case D:
                    cal.add(Calendar.DATE, count);
                    break;
                case M:
                    cal.add(Calendar.MONTH, count);
                    break;
                case Y:
                    cal.add(Calendar.YEAR, count);
                    break;
                default:
                    throw new ValidationException(InvalidFunctionArgument,
                            format("Unknown unit argument in %s merge field.", this.toString()));
            }
            return df.format(cal.getTime());
        } catch (ParseException e) {
            throw new ValidationException(InvalidFunctionArgument,
                    format("Function %s expects a date-typed input, but it received %s", NAME, content), e);
        }
    }

    @Override
    public String toString() {
        return format("%s(%d,%s)", NAME, count, unit.name());
    }
}
