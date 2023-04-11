package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.WithFieldArg;
import me.smilingleo.WithInputField;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.model.HierarchicalMap;
import me.smilingleo.utils.FieldUtils;
import me.smilingleo.utils.Asserts;
import me.smilingleo.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Concat will concat values of several fields together. For example:</p>
 * <code>
 * {{#Invoice}} {{.|Concat(Account.AccountNumber,InvoiceNumber,"_")}} {{/Invoice}}
 * </code>
 */
public final class Concat implements Decorator, WithInputField, WithFieldArg {

    private static final String NAME = "Concat";

    String delimiter;
    List<String> fields;

    private Concat(String delimiter, List<String> fields) {
        this.delimiter = delimiter;
        this.fields = fields;
    }

    public static Concat parse(String label) {
        List<String> list = StringUtils.parseFunctionArguments(label);
        Asserts.assertTrue(list.size() > 1, ErrorCode.InvalidFunctionArgument,
                format("Function %s expected at least two arguments. "
                                + "For example, to concat the first name and last name of the BillTo contact, "
                                + "you can do: {{Account.BillTo|%s(FirstName,LastName,'_')}}",
                        NAME, NAME));
        // last one is delimiter.
        String delimiter = list.get(list.size() - 1);
        List<String> fields = list.stream().limit(list.size() - 1).collect(Collectors.toList());
        return new Concat(delimiter, fields);
    }

    @Override
    public List<String> getArgFieldNames() {
        return fields.stream()
                .map(MergeFieldParser::parse)
                .map(field -> {
                    String fieldName = field.getInputFieldName();
                    return FieldUtils.getOriginalInputFieldName(fieldName).orElse(fieldName);
                })
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String label = format("%s(%s,%s)", NAME, fields.stream().collect(Collectors.joining(",")), delimiter);
        return label;
    }

    @Override
    public String getInputFieldName() {
        return WithInputField.CURRENT_OBJECT;
    }

    @Override
    public Object evaluate(Object input) {
        if (input == null) {
            return null;
        }
        Asserts.assertType(input, Map.class,
                format("Function %s expects a object type input, but it receives a %s.", NAME,
                        input.getClass().getSimpleName()));
        HierarchicalMap map = HierarchicalMap.fromMap((Map) input);
        return fields.stream()
                .map(fieldName -> {
                    MergeField mergeField = MergeFieldParser.parse(fieldName);
                    Object rawValue = map.findValue(mergeField.getInputFieldName());
                    Object value = mergeField.dataBind(rawValue).orElse(rawValue);
                    if (value != null) {
                        return value;
                    } else {
                        throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                                format("%s in %s is invalid", fieldName, this.toString()));
                    }
                })
                .map(Object::toString)
                .collect(Collectors.joining(StringUtils.unquote(delimiter)));
    }
}
