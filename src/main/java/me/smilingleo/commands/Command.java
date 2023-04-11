package me.smilingleo.commands;

import me.smilingleo.WithFieldArg;
import me.smilingleo.WithInputField;
import me.smilingleo.decorators.Decorator;

/**
 * <p>Command is an action performed on current context object.</p>
 *
 * <p>Command by itself can be a merge field, which is not like
 * {@link Decorator}</p>
 *
 * <p>Command returns no value, which means it has side-effect to the context object.
 * For example, {{#InvoiceItem}}{{Cmd_Assign(NewId,Id)}}, the `Cmd_Assign` command will
 * add a new attribute (NewId) to InvoiceItem object.</p>
 */
public interface Command extends WithInputField, WithFieldArg {

    /**
     * Command modifies the input object and returns nothing.
     */
    void execute(Object input);

}
