package me.smilingleo.functions;

import me.smilingleo.WithFieldArg;
import me.smilingleo.WithInputField;

import java.util.Map;

public interface Function extends WithInputField, WithFieldArg {

    /**
     * evaluate a function: `(context-object) -> result`
     * @param context context object.
     * @return
     */
    Object evaluate(Map<String, Object> context);
}
