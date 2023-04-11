package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.RenderContext;
import me.smilingleo.Variable;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.utils.StringUtils;

import me.smilingleo.utils.Asserts;

import java.util.List;
import java.util.Objects;

/**
 * <p>Compare the input with a variable, remember the comparison result,
 * and set the variable with current input before return the comparison result.</p>
 */
public final class EqualToVar implements Decorator, ReturningScalar {

    private static final String NAME = "EqualToVar";
    private String varName;

    private EqualToVar(String varName) {
        this.varName = varName;
    }

    public static EqualToVar parse(String label) {
        List<String> list = StringUtils.parseFunctionArguments(label);
        Asserts.assertTrue(list.size() == 1, ErrorCode.InvalidFunctionArgument,
                format("Invalid argument for function %s: %s", NAME, label));
        return new EqualToVar(list.get(0));
    }

    /**
     * @param input should be a scalar type.
     */
    @Override
    public Boolean evaluate(Object input) {
        Variable variable = RenderContext.getContext().getVariable(varName);
        Object variableValue = variable == null ? null : variable.getEvaluatedValue();
        boolean equals = Objects.equals(input, variableValue);

        RenderContext.getContext().assignVariable(varName, input);
        return equals;
    }

    @Override
    public String toString() {
        return format("%s(%s)", NAME, varName);
    }
}
