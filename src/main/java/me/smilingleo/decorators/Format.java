package me.smilingleo.decorators;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static java.lang.String.format;

import me.smilingleo.RenderContext;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.FormatUtils;
import me.smilingleo.utils.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class Format implements Decorator {
    private static final String NAME = "Format";

    private String format;

    private Format(String format) {
        this.format = format;
    }

    public static Format parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        assertTrue(!arguments.isEmpty(), InvalidFunctionArgument,
                format("Function %s takes argument.", NAME));
        int openIndex = label.indexOf('(');
        String content = label.substring(openIndex + 1, label.length() - 1);
        return new Format(content);
    }

    @Override
    public String evaluate(Object input)  {
        if (input == null) {
            return "";
        }
        String data = input.toString();
        int length = data.length();
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.format, getLocale());
            if (length == 10) {
                LocalDate localDate = LocalDate.parse(data);
                return dateTimeFormatter.format(localDate);
            }

            ZonedDateTime zonedDateTime = ZonedDateTime.parse(input.toString());

            if (ZoneId.getAvailableZoneIds().contains(zonedDateTime.getZone().toString())
                    || Objects.nonNull(ZoneId.SHORT_IDS.get(zonedDateTime.getZone().toString()))) {
                return dateTimeFormatter.format(zonedDateTime);
            } else {
                return dateTimeFormatter.withZone(ZoneId.of(getTimeZone())).format(zonedDateTime);
            }

        } catch (DateTimeParseException e) {
            throw new ValidationException(InvalidFunctionArgument, format("Invalid value:%s for function %s", input, NAME), e);
        } catch (DateTimeException e) {
            throw new ValidationException(InvalidFunctionArgument, format("Couldn't Format the input:%s with format:%s", input, this.format), e);
        }

    }

    @Override
    public String toString() {
        return StringUtils.notNullOrBlank(format) ? format("%s(%s)", NAME, format) : NAME;
    }

    public Locale getLocale() {
        String localeFromContext = RenderContext.getContext().getLocale();
        return FormatUtils.fromString(localeFromContext);
    }

    private String getTimeZone() {
        return RenderContext.getContext().getTimeZone();
    }

}
