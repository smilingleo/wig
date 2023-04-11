package me.smilingleo.utils;

import static java.lang.String.format;

import me.smilingleo.RenderContext;
import me.smilingleo.Variable;

import java.util.Optional;

public class FieldUtils {

    /**
     * Given a fieldName, try to find its original name defined in metadata.
     *
     * @return Optional.empty() if the fieldName is
     * <ul>
     *     <li>_Group or its alias</li>
     *     <li>an alias of a decorated merge field.</li>
     *     <li>global variable</li>
     *     <li>wrapper function</li>
     *     </ul>
     */
    public static Optional<String> getOriginalInputFieldName(String fieldName) {
        if (fieldName.equals(Constants.DERIVED_LIST_KEY)) {
            return Optional.empty();
        }
        String originalFieldName = fieldName;
        RenderContext renderContext = RenderContext.getContext();
        if (renderContext.isVariable(fieldName)) {
            Variable variable = renderContext.getVariable(fieldName);
            if (variable.isScalar()) {
                return Optional.empty();
            }
            originalFieldName = variable.getMergeField().getInputFieldName();
            if (originalFieldName.equals(Constants.DERIVED_LIST_KEY)) {
                return Optional.empty();
            } else if (originalFieldName.contains("|")) {
                // in case the original field is a merge field with decorator
                // it's an evaluation case, like {{Cmd_Assign(MaxAmount,Invoices|Max(Amount))}}
                // its data path has been handled by command function.
                // and the context of the merge field has also changed.
                return Optional.empty();
            }
        } else if (originalFieldName.startsWith(Constants.WRAPPER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(originalFieldName);
    }
}
