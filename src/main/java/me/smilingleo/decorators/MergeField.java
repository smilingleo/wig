package me.smilingleo.decorators;

import static java.lang.String.format;

import me.smilingleo.WithFieldArg;
import me.smilingleo.WithInputField;
import me.smilingleo.utils.Constants;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MergeField implements WithInputField, WithFieldArg {

    /**
     * This is the field name before any decorator, for example: `InvoiceItems` for `InvoiceItems|First(5)`.
     */
    private String fieldName;
    private Optional<List<Decorator>> decorators = Optional.empty();

    public MergeField(String fieldName, Decorator... decorators) {
        this.fieldName = fieldName;
        if (decorators != null && decorators.length > 0) {
            this.decorators = Optional.of(Arrays.asList(decorators));
        }
    }

    @Override
    public String getInputFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return fieldName + decorators.map(
                list -> "|" + list.stream()
                        .map(decorator -> decorator.toString())
                        .collect(Collectors.joining("|")))
                .orElse("");
    }

    public Optional<List<Decorator>> getDecorators() {
        return decorators;
    }

    /**
     * Return a list of argument names. In case of chained decorators, from the metadata hierarchy point of view, there
     * are two scenarios:
     * <ul>
     *   <li>sibling arguments, for example: `InvoiceItems|FilterByValue(ChargeAmount,GT,0)|FilterByValue(ChargeModel,EQ,OneTime)`,
     *      <br/>we need to return [ChargeAmount,ChargeModel].</li>
     *   <li>Hierarchical arguments, for example:<br>
     *       `InvoiceItems|FlatMap(TaxationItems)|GroupBy(Name,TaxRateType)` where we should return
     *      `[TaxationItems.Name,TaxationItems.TaxRateType]`</li>
     *   <li>Hybrid scenarios
     *   `InvoiceItems|FilterByValue(ChargeAmount,GT,0)|FlatMap(TaxationItems)|GroupBy(Name,TaxRateType)`<br/>
     *   In this case, we need to return [ChargeAmount,TaxationItems.Name,TaxationItems.TaxRateType].</li>
     * </ul>
     *
     * @return array of argument field names
     */
    @Override
    public List<String> getArgFieldNames() {
        List<String> list = new LinkedList<>();
        decorators.ifPresent(functions -> {
            String contextObject = "";
            for (Decorator decorator : functions) {
                if (!(decorator instanceof WithFieldArg)) {
                    continue;
                }
                List<String> argFieldNames = ((WithFieldArg) decorator).getArgFieldNames();
                if (Constants.SWITCH_CONTEXT_DECORATORS.contains(decorator.getClass())) {
                    // it could be empty in case of FlatMap(_Group)
                    if (argFieldNames.size() == 1) {
                        String newContextObject = argFieldNames.get(0);
                        contextObject = contextObject.isEmpty() ? newContextObject
                                : format("%s.%s", contextObject, newContextObject);
                        list.add(contextObject);
                    } else if (argFieldNames.size() > 1) {
                        // Map decorator supports multiple arguments, in which case, it's not a context switch.
                        appendArgFieldNames(list, contextObject, argFieldNames);
                    }
                } else {
                    appendArgFieldNames(list, contextObject, argFieldNames);
                }
            }
        });
        return list;
    }

    private void appendArgFieldNames(List<String> list, String contextObject, List<String> argFieldNames) {
        for (String argField : argFieldNames) {
            if (contextObject.isEmpty()) {
                list.add(argField);
            } else {
                list.add(contextObject + "." + argField);
            }
        }
    }

    public Optional<Object> dataBind(Object input) {
        return decorators.map(list -> {
            AtomicReference ref = new AtomicReference(input);
            // Account.Invoices|Sum(Balance), Account.BillTo.WorkEmail|Substr(0,10)
            list.forEach(decorator -> ref.set(decorator.evaluate(ref.get())));
            return ref.get();
        });
    }
}
