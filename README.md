# Wig
An enhanced Mustache template engine.

## Why Wig?
Mustache is a logic-less template engine. It is a great tool for generating HTML, but it is not very flexible. Wig is a Mustache template engine that adds some functions to transform data and control the flow of the template.

## Usage
```java
String template = "Hello {{Date|Localise(en_US)}}!";
Map<String, Object> data = new HashMap<>();
data.put("Date", "2023-04-10");

Wig wig = new Wig();
wig.render(template, data); // you will get `Hello 04/10/2023!`
```
