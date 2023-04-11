package me.smilingleo.functions;

import static java.lang.String.format;

import me.smilingleo.RenderContext;
import me.smilingleo.Variable;
import me.smilingleo.decorators.MergeFieldParser;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.FormatUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

/**
 *
 */
public final class Today implements Function {

    private static final String NAME = "Fn_Today()";
    private String label;

    private Today() {
        RenderContext renderContext = RenderContext.getContext();
        // if there is no Fn_Today() variable defined, create one.
        if (!renderContext.isVariable(NAME)) {
            String tz = renderContext.getTimeZone();
            // parse tz to return a timezone object, if tz is null, return system timezone.
            TimeZone timezone = Optional.ofNullable(tz).map(TimeZone::getTimeZone).orElse(TimeZone.getDefault());
            Calendar cal = Calendar.getInstance(timezone);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
                    FormatUtils.fromString(renderContext.getLocale()));
            dateFormat.setTimeZone(timezone);
            String todayStr = dateFormat.format(cal.getTime());
            renderContext.registerVariable(NAME, new Variable(NAME, "'" + todayStr + "'", true));
        }
    }

    public static Today parse(String label) {
        if (NAME.equals(label) || label.startsWith(NAME + "|")) {
            Today today = new Today();
            today.label = label;
            return today;
        } else {
            throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                    format("Invalid argument for %s function: %s. %s accepts no argument.", NAME, label, NAME));
        }
    }

    @Override
    public Object evaluate(Map<String, Object> context) {
        RenderContext renderContext = RenderContext.getContext();
        Object todayValue = renderContext.getVariable(NAME).getEvaluatedValue();
        return MergeFieldParser.parse(this.label).dataBind(todayValue).orElse(todayValue);
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public List<String> getArgFieldNames() {
        return Collections.emptyList();
    }

    @Override
    public String getInputFieldName() {
        return NAME;
    }
}
