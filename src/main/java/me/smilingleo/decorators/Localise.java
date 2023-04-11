package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static java.lang.String.format;

import me.smilingleo.RenderContext;
import me.smilingleo.utils.FormatUtils;
import me.smilingleo.utils.StringUtils;

import java.util.List;

/**
 * <p>A decorator to format a numeric or date/datetime value.</p>
 *
 * <p>A locale argument can be used to specify the format,
 * if no argument is used, default locale will be used.</p>
 */
public final class Localise implements Decorator, ReturningScalar {

    private static final String NAME = "Localise";
    private String locale;

    private Localise(String locale) {
        this.locale = locale;
    }

    public static Localise parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(arguments.size() <= 1, InvalidFunctionArgument,
                format("Function %s takes no or one argument.", NAME));
        String content = arguments.isEmpty() ? "" : arguments.get(0);
        return new Localise(content);
    }

    @Override
    public String evaluate(Object input) {
        if (input == null) {
            return "";
        }
        assertTrue(input instanceof String || input instanceof Number, InvalidFunctionArgument,
                format("Function %s expects a text or numeric type input, but it receives a %s", NAME,
                        input.getClass().getSimpleName()));
        boolean isNumber = isNumber(input);
        return isNumber ? FormatUtils.localiseNumber(input.toString(), getLocale())
                : FormatUtils.localiseDateTime(input.toString(), getLocale());
    }

    public String getLocale() {
        return StringUtils.notNullOrBlank(locale) ? locale : RenderContext.getContext().getLocale();
    }

    @Override
    public String toString() {
        return StringUtils.notNullOrBlank(locale) ? format("%s(%s)", NAME, locale) : NAME;
    }

    private boolean isNumber(Object input) {
        if (input instanceof Number) {
            return true;
        }
        return StringUtils.isNumber(input.toString());
    }

}
