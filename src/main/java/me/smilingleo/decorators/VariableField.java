package me.smilingleo.decorators;

import me.smilingleo.RenderContext;
import me.smilingleo.Variable;

import java.util.Collections;
import java.util.List;

public final class VariableField extends MergeField {

    private Variable variable;

    public VariableField(String fieldName) {
        super(fieldName);
        this.variable = RenderContext.getContext().getVariable(fieldName);
    }

    @Override
    public String getInputFieldName() {
        return variable == null || variable.isScalar() ? "" : variable.getMergeField().getInputFieldName();
    }

    @Override
    public List<String> getArgFieldNames() {
        return variable == null || variable.isScalar() ? Collections.emptyList()
                : variable.getMergeField().getArgFieldNames();
    }

    public String getVariableName() {
        return super.getInputFieldName();
    }

    public Variable getVariable() {
        return variable;
    }
}
