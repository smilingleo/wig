package me.smilingleo.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReflectionUtils {

    public static <T> T invokeParse(Class<T> decoratorClass, String label) {
        Method[] methods = decoratorClass.getDeclaredMethods();
        for (Method method : methods) {
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            boolean sameName = "parse".equals(method.getName());
            if (sameName && isStatic) {
                try {
                    Object returned = method.invoke(decoratorClass, label);
                    if (returned != null) {
                        return (T) returned;
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(String.format("Failed to invoke parse method of %s, because %s",
                            decoratorClass.getSimpleName(),
                            e.getMessage() == null ? e.getCause().getMessage() : e.getMessage()), e);
                }
            }
        }
        throw new IllegalStateException("No parse method found for class: " + decoratorClass.getSimpleName());
    }
}
