package me.smilingleo.commands;

import static me.smilingleo.exceptions.ErrorCode.InvalidCommandArgument;
import static me.smilingleo.utils.Asserts.assertTrue;
import static me.smilingleo.utils.Asserts.assertType;
import static java.lang.String.format;

import me.smilingleo.RenderContext;
import me.smilingleo.Variable;
import me.smilingleo.model.HierarchicalMap;
import me.smilingleo.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>In a multi-level section stack, according to Mustache spec, the tag resolution goes from inside to outside,
 * if the inside object and its outer object have same attribute name, like Id, and the inside object also has need to
 * reference the outer object id, we have to find a way to differentiate those two Ids.</p>
 * <p>For example, see the following example:</p>
 * <pre>
 * Regular invoice items
 * {{#InvoiceItems|Filter(ProcessingType,EQ,0)}}
 *     Filter discount items by `Id` of regular item.
 *     {{ChargeName}} - {{ChargeAmount}} - {{InvoiceItems|Filter(AppliedToInvoiceItemId,EQ,Id)|Sum}}
 * {{/InvoiceItems|Filter(ProcessingType,EQ,0)}}
 * </pre>
 * <p>The `Id` in the `Filter` decorator argument should be the Id of regular invoice item, aka, the outer object.
 * But with the same name `Id`, Mustache engine will resolve it as the Id of discount items, aka,
 * `{{InvoiceItems|Filter(xxx)}}`</p>
 *
 * <p>This command gives the Id of outer object a different name, for the same example, we can do:</p>
 * <pre>
 * Regular invoice items
 * {{#InvoiceItems|Filter(ProcessingType,EQ,0)}}
 *     {{Cmd_Assign(RegularItemId,Id)}}
 *     Filter discount items by `Id` of regular item.
 *     {{ChargeName}} - {{ChargeAmount}} - {{InvoiceItems|Filter(AppliedToInvoiceItemId,EQ,RegularItemId)|Sum}}
 * {{/InvoiceItems|Filter(ProcessingType,EQ,0)}}
 * </pre>
 *
 * <p>Cmd_Assign can create `global variables` as well as local variables,aka, `aliases`.</p>
 *
 * <h3>Rules for Global Variables</h3>
 * <p>You can create a global variable by `{{Cmd_Assign(VarCurrency,Invoice.Account.Currency|Symbol,True)}}`,
 * and then you can use `{{VarCurrency}}` anywhere in the template.</p>
 *
 * <h3>Rules for Local Variables/Aliases</h3>
 * <p>By default(without explicit True flag), the variable is defined in the current contextual scope.</p>
 * <p>Due to the fact that the variable is appended to the outer object, so only the children merge fields
 * can access that variable. For example, you can't define a variable at InvoiceItem level but use it at Invoice level,
 * like:</p>
 * <pre>
 *  {{#Invoice}}
 *  {{InvoiceItems|FilterByRef(ChargeName,EQ,VarName)|Size}} // VarName is not accessible here.
 *   {{#InvoiceItems}}
 *       {{Cmd_Assign(VarName,ChargeName)}}   // define a variable on InvoiceItem
 *   {{/InvoiceItems}}
 *  {{/Invoice}}
 * </pre>
 * <p>The other fact is that the variable does not exist by it own, it has to exist under an object.
 * If you use this command in a loop, there will be one variable being appended to each item, for example:</p>
 * <pre>
 *  {{#Invoice}}
 *   {{#InvoiceItems}}                      // Each InvoiceItem will contain a variable named `VarName`.
 *       {{Cmd_Assign(VarName,ChargeName)}}
 *   {{/InvoiceItems}}
 *  {{/Invoice}}
 * </pre>
 */
public final class Assign implements Command {

    private static final String NAME = "Cmd_Assign";

    private Variable variable;

    private Assign(String variableName, String expression, String globalFlag) {
        assertTrue(variableName != null && variableName.trim().length() > 0, InvalidCommandArgument,
                "Variable name can not be blank");
        assertTrue(variableName.indexOf('.') < 0, InvalidCommandArgument,
                format("Variable name can not be dotted path, '%s' is invalid.", variableName));

        boolean global = Boolean.parseBoolean(globalFlag);
        this.variable = new Variable(variableName, expression, global);

        // It implies we don't allow duplicate names globally.
        // does this make sense?
        RenderContext.getContext().registerVariable(variableName, this.variable);
    }

    public static Assign parse(String label) {
        List<String> arguments = StringUtils.parseFunctionArguments(label);
        int argSize = arguments.size();
        assertTrue(argSize == 2 || argSize == 3, InvalidCommandArgument,
                format("Command %s expected two arguments. For example, {{%s(VarName,ChargeName)}} "
                                + "will assign ChargeName to a variable named VarName."
                                + "{{%s(VarName,ChargeName,True)}} will define a global variable.",
                        NAME, NAME, NAME));

        String field1 = arguments.get(0);
        String field2 = arguments.get(1);
        String global = argSize == 3 ? arguments.get(2) : null;
        return new Assign(field1, field2, global);
    }

    @Override
    public void execute(Object input) {
        if (input == null) {
            return;
        }
        assertType(input, Map.class,
                format("Command %s expects an object type input, but it received a %s.", NAME,
                        input.getClass().getSimpleName()));
        Map<String, Object> typedInput = (Map<String, Object>) input;
        Object value = null;
        if (variable.isScalar()) {
            value = StringUtils.unquote(variable.getExpression());
        } else {
            value = HierarchicalMap.fromMap(typedInput).findValue(variable.getMergeField().getInputFieldName());
            value = variable.getMergeField().dataBind(value).orElse(value);
        }
        variable.setEvaluatedValue(value);

        if (!variable.isGlobal()) {
            // use `put` here to be able to override existing data.
            typedInput.put(variable.getName(), value);
        } else {
            // for global variable, we need to make it available at transform phase, so that it can be used with functions.
            // just find the ultimate root and append it as a property.
            if (typedInput instanceof HierarchicalMap) {
                HierarchicalMap current = (HierarchicalMap) typedInput;
                HierarchicalMap parent = current.getParent();
                int depthCounter = 0;
                while (parent != null && depthCounter < 1000) {
                    current = parent;
                    parent = parent.getParent();
                    depthCounter++;
                }
                if (parent == null) {
                    // There should be validation else where to prevent the variable override the standard object names
                    current.put(variable.getName(), value);
                }
            }
        }
    }

    @Override
    public String toString() {
        return variable.isGlobal() == null
                ? format("%s(%s,%s)", NAME, variable.getName(), variable.getExpression())
                : format("%s(%s,%s,%s)", NAME, variable.getName(), variable.getExpression(), variable.isGlobal());
    }

    @Override
    public List<String> getArgFieldNames() {
        return variable.isScalar() ? Collections.emptyList() : variable.getMergeField().getArgFieldNames();
    }

    @Override
    public String getInputFieldName() {
        return variable.isScalar() ? "" : variable.getMergeField().getInputFieldName();
    }

    public String getVariableName() {
        return variable.getName();
    }

    public Variable getVariable() {
        return variable;
    }
}
