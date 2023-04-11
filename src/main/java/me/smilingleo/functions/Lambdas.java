package me.smilingleo.functions;

import static me.smilingleo.utils.Constants.WRAPPER_PREFIX;
import static me.smilingleo.utils.StringUtils.isNullOrBlank;
import static me.smilingleo.utils.StringUtils.notNullOrBlank;
import static java.lang.String.format;

import me.smilingleo.RenderContext;
import me.smilingleo.decorators.Decorator;
import me.smilingleo.decorators.MergeFieldParser;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.Tuple;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * See the lambdas section on page of https://mustache.github.io/mustache.5.html
 */
public enum Lambdas {
    Eval {
        @Override
        public java.util.function.Function<String, String> function() {
            return s -> {
                Tuple<List<String>> tuple = findDecoratorsInExpr(s);
                String exprStr = tuple.getLeft();
                List<String> decorators = tuple.getRight();
                try {
                    JexlExpression expr = engine.createExpression(exprStr);
                    MapContext context = new MapContext();
                    RenderContext.getContext().getVariables().values().stream()
                            .filter(variable -> variable.isGlobal())
                            .forEach(globalVariable -> context.set(globalVariable.getName(), globalVariable.getEvaluatedValue()));
                    Object evaluated = expr.evaluate(context);
                    String output = evaluated == null ? "" : evaluated.toString();
                    List<Decorator> functions = MergeFieldParser.parseDecorators(
                            String.join("|", decorators));
                    Object value = output;
                    for (Decorator decorator : functions) {
                        value = decorator.evaluate(value);
                    }
                    return value.toString();
                } catch (JexlException je) {
                    throw new ValidationException(ErrorCode.InvalidExpression,
                            format("'%s' is an invalid expression.", exprStr), je);
                }

            };
        }
    };

    public abstract java.util.function.Function<String, String> function();

    public static Map<String, Function<String, String>> allLambdas() {
        return Stream.of(Lambdas.values())
                .map(lambda -> new SimpleEntry<String, java.util.function.Function>(WRAPPER_PREFIX + lambda.name(),
                        lambda.function()))
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    private static JexlEngine engine = new JexlBuilder().create();

    private static Tuple<List<String>> findDecoratorsInExpr(String expr) {
        if (isNullOrBlank(expr)) {
            return new Tuple(expr, Collections.emptyList());
        }
        String content = expr.trim();
        List<String> decorators = new LinkedList<>();
        int pos = content.lastIndexOf('|');
        while (pos >= 0) {
            String decorator = content.substring(pos + 1);
            if (notNullOrBlank(decorator) && !decorator.contains(" ")) {
                decorators.add(0, decorator);
            }
            content = content.substring(0, pos);
            pos = content.lastIndexOf('|');
        }
        return new Tuple(content, decorators);
    }
}
