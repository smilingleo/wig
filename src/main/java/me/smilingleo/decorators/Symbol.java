package me.smilingleo.decorators;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public final class Symbol implements Decorator, ReturningScalar {

    private static final String CURRENCY_CODE_SYMBOL_MAP_FILE = "currency-code-symbol.properties";
    private static final Map<String, String> CURRENCY_CODE_SYMBOL_MAP = new HashMap<>();

    static {
        loadCurrencyCodeSymbol();
    }

    private Symbol() {
    }

    public static Symbol parse(String label) {
        if (Objects.equals("Symbol", label)) {
            return new Symbol();
        }
        if (label.startsWith("Symbol(")) {
            throw new ValidationException(ErrorCode.InvalidFunctionArgument,
                    "Symbol decorator takes no argument.");
        }
        throw new ValidationException(ErrorCode.InvalidFunctionArgument, "Invalid name for Symbol decorator.");
    }

    @Override
    public Object evaluate(Object input) {
        if (input == null) {
            return "";
        }
        // if the input is not currency code, return the input as is
        return CURRENCY_CODE_SYMBOL_MAP.containsKey(input.toString()) ? CURRENCY_CODE_SYMBOL_MAP.get(input.toString())
                : input;
    }

    @Override
    public String toString() {
        return "Symbol";
    }


    private static void loadCurrencyCodeSymbol() {
        try {
            try (InputStreamReader is = new InputStreamReader(
                    Symbol.class.getResourceAsStream(CURRENCY_CODE_SYMBOL_MAP_FILE), StandardCharsets.UTF_8)) {
                Properties props = new Properties();
                props.load(is);
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    CURRENCY_CODE_SYMBOL_MAP.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
