package me.smilingleo.decorators;

/**
 * <p>A decorator is a function applied to a data field, by itself, it can NOT be a merge field,
 * it has to appear together with a data field. For example: `{{#InvoiceItems|Filter(xxx)}}`,
 * wherein, `InvoiceItems` is the data field, `Filter` is a decorator. A `|` is required
 * between data field and decorator.</p>
 *
 * <p>Decorators can be chained, for example: `{{#InvoiceItems|Filter(xxs)|Filter(xxx)|GroupBy(xx)}}`</p>
 */
public interface Decorator {
    Object evaluate(Object input);
}
