package me.smilingleo;

import static me.smilingleo.exceptions.ErrorCode.InvalidValue;

import me.smilingleo.decorators.MergeField;
import me.smilingleo.decorators.MergeFieldParser;
import me.smilingleo.utils.StringUtils;

import me.smilingleo.utils.Asserts;

import java.util.Objects;

public class Variable {

    private String name;
    /**
     * if an expression is quoted/double-quoted, it's a scalar value.
     */
    private String expression;
    private Boolean global;

    private boolean scalar;
    private MergeField mergeField;
    private Object evaluatedValue;

    public Variable(String name, String expression, boolean global) {
        Asserts.assertTrue(name.indexOf(' ') < 0, InvalidValue,
                "Variable name can not contain whitespace, but you have '" + name + "'");
        this.name = name;

        Asserts.assertTrue(expression != null, InvalidValue, "You have to assign a not-null value to variable '" + name + "'");
        this.expression = expression;
        if (StringUtils.isText(expression)) {
            scalar = true;
            evaluatedValue = StringUtils.unquote(expression);
        } else {
            mergeField = MergeFieldParser.parse(expression);
            // we don't know the evaluated value yet when constructing the variable.
            // we can only know at the data transformation phase.
        }

        this.global = global;
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public Boolean isGlobal() {
        return global;
    }

    public boolean isScalar() {
        return scalar;
    }

    public MergeField getMergeField() {
        return mergeField;
    }

    public Object getEvaluatedValue() {
        return evaluatedValue;
    }

    public void setEvaluatedValue(Object evaluatedValue) {
        this.evaluatedValue = evaluatedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Variable variable = (Variable) o;
        return Objects.equals(global, variable.global) && name.equals(variable.name) && expression.equals(variable.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, expression, global);
    }
}
