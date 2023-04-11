package me.smilingleo.functions;

import static me.smilingleo.utils.StringUtils.isNumber;
import static me.smilingleo.utils.StringUtils.isText;
import static java.lang.String.format;

import me.smilingleo.decorators.MergeField;
import me.smilingleo.decorators.MergeFieldParser;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Function `Add` takes in two arguments and generate one output.</p>
 * <p>The first argument MUST be a merge field, and the second one can be either a value or another merge field.</p>
 * <p>The data type of second argument must be consistent with the data type of the first merge field, e.g..</p>
 * <ul>
 *     <li>If first argument is a numeric field, the second one must be a numeric value or field too.
 *     For numeric values, just sum them up.</li>
 *     <li>If the arguments are text, the result is the concat of the two strings. If the second argument is a text,
 *     it has to 1) escape the whitespace, 2) quoted.</li>
 * </ul>
 */
public final class Add implements Function {

    private static final String NAME = "Fn_Add";
    private static final Pattern PATTERN = Pattern.compile(NAME + "\\(([\\w|\\.]+),([^)]+)\\)");

    private MergeField left;
    /**
     * The type of right argument is uncertain, can only determine at runtime by checking the first.
     */
    private String right;

    private Add(MergeField left, String right) {
        this.left = left;
        this.right = right;
    }

    public static Add parse(String label) {
        Matcher matcher = PATTERN.matcher(label);
        if (matcher.matches()) {
            String fieldStr = matcher.group(1);
            MergeField mergeField = MergeFieldParser.parse(fieldStr);
            String rightStr = matcher.group(2);
            if (!isText(rightStr) && rightStr.contains(",")) {
                throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                        format("Too many arguments for %s function: %s. %s accepts two arguments.", NAME, label, NAME));
            }
            return new Add(mergeField, rightStr);
        } else {
            throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                    format("Invalid argument for %s function: %s. %s accepts two arguments.", NAME, label, NAME));
        }
    }

    @Override
    public Object evaluate(Map<String, Object> context) {
        // if the context object is null/empty
        boolean rightIsScalar = isNumber(right) || isText(right);
        if ((context == null || context.isEmpty()) && rightIsScalar) {
            return right;
        }
        Object leftValue = evaluateMergeField(context, left);
        Object rightValue = evaluateRightValue(context, rightIsScalar);
        if (leftValue == null) {
            return rightValue;
        }
        // leftValue is not null.
        if (rightValue == null) {
            return leftValue;
        }
        // neither left nor right is null now.
        return add(leftValue, rightValue);
    }

    private Object add(Object leftValue, Object rightValue) {
        if (isNumber(leftValue.toString())) {
            if (isNumber(rightValue.toString())) {
                return Double.parseDouble(leftValue.toString()) + Double.parseDouble(rightValue.toString());
            } else {
                throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                        format("Right argument of %s should be a number, but it's %s", this.toString(),
                                rightValue.getClass().getSimpleName()));
            }
        } else {
            return leftValue.toString() + rightValue.toString();
        }
    }

    private Object evaluateMergeField(Map<String, Object> context, MergeField mergeField) {
        Object inputValue = MapUtils.getByDottedPath(context, mergeField.getInputFieldName());
        return mergeField.dataBind(inputValue).orElse(inputValue);
    }

    private Object evaluateRightValue(Map<String, Object> context, boolean rightIsScalar) {
        if (rightIsScalar) {
            return StringUtils.unquote(right);
        }
        // it's not a scalar, but a merge field
        try {
            MergeField rightField = MergeFieldParser.parse(right);
            return evaluateMergeField(context, rightField);
        } catch (Exception e) {
            throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                    format("Right argument of %s must be either a valid merge field or a quoted-text or a numeric value.",
                            NAME), e);
        }
    }

    @Override
    public String toString() {
        return format("%s(%s,%s)", NAME, left, right);
    }

    @Override
    public List<String> getArgFieldNames() {
        List<String> list = new LinkedList<>();
        list.add(left.getInputFieldName());
        if (!isText(right) && !isNumber(right)) {
            MergeField rightField = MergeFieldParser.parse(right);
            list.add(rightField.getInputFieldName());
        }
        return list;
    }

    @Override
    //TODO: conceptually, Command and Function should not implement WithInputField
    // since there is no input field at left side with a pipe like a decorator function does
    // Just implement as so to make it work, will refactor this later.
    public String getInputFieldName() {
        return left.getInputFieldName();
    }
}
