package me.smilingleo.functions;

import static me.smilingleo.exceptions.ErrorCode.InvalidFunctionArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.StringUtils.isBoolean;
import static me.smilingleo.utils.StringUtils.isNumber;
import static me.smilingleo.utils.StringUtils.isText;
import static me.smilingleo.utils.StringUtils.parseFunctionArguments;
import static java.lang.String.format;

import me.smilingleo.decorators.MergeField;
import me.smilingleo.decorators.MergeFieldParser;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public final class Calc implements Function {

    private static final String NAME = "Fn_Calc";

    private MergeField left;

    private CalcOperator operator;
    /**
     * The type of right argument is uncertain, can only determine at runtime by checking the first.
     */
    private String right;

    private Calc(MergeField left, CalcOperator operator, String right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public static Calc parse(String label) {
        List<String> arguments = parseFunctionArguments(label);
        assertTrue(arguments.size() == 3, InvalidFunctionArgument,
                format("function %s expects %d arguments but it received %d.", NAME, 3, arguments.size()));

        String fieldStr = arguments.get(0);
        MergeField mergeField = MergeFieldParser.parse(fieldStr);
        String operator = arguments.get(1);
        String rightStr = arguments.get(2);
        if (!isText(rightStr) && rightStr.contains(",")) {
            throw new ValidationException(InvalidFunctionArgument,
                    format("Too many arguments for %s function: %s. %s accepts two arguments.", NAME, label, NAME));
        }
        return new Calc(mergeField, CalcOperator.valueOf(operator), rightStr);
    }

    @Override
    public Object evaluate(Map<String, Object> context) {
        // if the context object is null/empty
        boolean rightIsScalar = isBoolean(right) || isNumber(right) || isText(right);
        if ((context == null || context.isEmpty()) && rightIsScalar) {
            return right;
        }
        Object leftValue = evaluateMergeField(context, left);
        Object rightValue = evaluateRightValue(context, rightIsScalar);
        return operator.calulate(leftValue, rightValue);
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
            throw new ValidationException(InvalidFunctionArgument,
                    format("Right argument of %s must be either a scalar value.", NAME), e);
        }
    }

    @Override
    public String toString() {
        return format("%s(%s,%s,%s)", NAME, left, operator, right);
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
    public String getInputFieldName() {
        return left.getInputFieldName();
    }
}
