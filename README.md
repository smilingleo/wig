# Wig
![[logo]](./logo.png)

An enhanced Mustache template engine.

## Why Wig?
Mustache is a logic-less template engine. It is a great tool for generating HTML, but it is not very flexible. Wig is a Mustache template engine that adds some functions to transform data and control the flow of the template.

## Usage
```java
String template = """
        {{#Invoice}}
            Invoices with charges greater than $100:
            {{#InvoiceItems|FilterByValue(ChargeAmount,GT,100)}}
                {{ChargeName}} - {{ChargeAmount}}
            {{/InvoiceItems|FilterByValue(ChargeAmount,GT,100)}}

            Total charge amount by charge name:
            {{#InvoiceItems|GroupBy(ChargeName)}}
                {{ChargeName}} - {{_Group|Sum(ChargeAmount)|Round(2)|Localise}}
            {{/InvoiceItems|GroupBy(ChargeName)}}
        {{/Invoice}}
        """;
Map<String, Object> data = Map.of(
        "Invoice", Map.of(
                "InvoiceItems", List.of(
                        Map.of("ChargeName", "Charge 1", "ChargeAmount", 100, "ServiceDate", "2021-01-01"),
                        Map.of("ChargeName", "Charge 1", "ChargeAmount", 150, "ServiceDate", "2021-02-01"),
                        Map.of("ChargeName", "Charge 2", "ChargeAmount", 200, "ServiceDate", "2021-01-01"),
                        Map.of("ChargeName", "Charge 2", "ChargeAmount", 250, "ServiceDate", "2021-02-01")
                )
        )
);
Wig wig = new Wig();
System.out.println(wig.render(template, data));
```

## Decorator
A decorator is a function that takes its left side as input and returns a value. Decorators are used to transform data. 

Decorators cannot be used standalone, they must be used with an input. For example:
```java
var template = "{{Name|Substr(0,3)}}";
var data = Map.of("Name", "John");
```

## Built-in Decorators
- Concat
- Default
- Format
- GroupBy
- Localise
- Map
- FlatMap
- Max
- Min
- Sum
- Uniq
- Round
- SortBy
- FilterByValue
- FilterByRef
- Symbol
- DateAdd
- EqualToVal
- IsBlank
- IsEmpty
- Last
- Size
- First
- Last
- Nth
- Skip


## Function
Different from decorators, functions can be used standalone. For example:
```
{{Fn_Today}}
```
Functions are named with the prefix `Fn_`.

### Built-in Functions
- Fn_Today
- Fn_Calc

## Command
Different from decorators and functions, commands have no output, they are used to declare variables or construct new objects.

See [test cases](./src/test/resources/data-driven/decorators/test-cases.yml) for more examples.
