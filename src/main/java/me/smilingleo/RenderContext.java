package me.smilingleo;

import static me.smilingleo.exceptions.ErrorCode.NotImplemented;
import static java.lang.String.format;

import me.smilingleo.utils.MapUtils;

import me.smilingleo.utils.Asserts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>Context information for parse process.</p>
 */
public class RenderContext {

    private static ThreadLocal<RenderContext> contextCache = new InheritableThreadLocal<RenderContext>() {
        @Override
        public RenderContext initialValue() {
            return new RenderContext();
        }
    };

    private String locale = "en_US";

    private String timeZone = "";

    // key is the object name, value is the attribute list.
    private Map<String, Set<String>> newObjects = new HashMap<>();
    private Map<String, Variable> variables = new HashMap<>();
    private Map<String, Object> debugInfo = new HashMap<>();
    private boolean debugMode = false;

    /**
     * The counter is the next index.
     */
    private Map<DebugInfoKey, Integer> stopwatchCounter = new HashMap<>();

    public RenderContext() {
    }

    public void addNewObject(String newObjectName) {
        this.newObjects.putIfAbsent(newObjectName, new HashSet<>());
    }

    public void registerNewField(String newObjectName, String newFieldName) {
        this.newObjects.computeIfPresent(newObjectName, (key, value) -> {
            this.newObjects.get(key).add(newFieldName);
            return this.newObjects.get(key);
        });
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Variable getVariable(String varName) {
        return variables.get(varName);
    }

    public boolean isVariable(String varName) {
        // Variable has to be case-sensitive
        return variables.containsKey(varName);
    }

    public Map<String, Variable> getVariables() {
        return variables;
    }

    public void registerVariable(String varName, Variable variable) {
        variables.put(varName, variable);
    }

    /**
     * Assign a value to a variable.
     * If the variable is not defined, create one on the fly.
     *
     * @param varName
     * @param value
     */
    public void assignVariable(String varName, Object value) {
        Variable variable = MapUtils.getIgnoreCase(variables, varName);
        if (variable != null) {
            variable.setEvaluatedValue(value);
        } else {
            // for EqualToVar use case, will create a variable on the fly.
            Variable newVar = new Variable(varName, "''", false);
            newVar.setEvaluatedValue(value);
            variables.put(varName, newVar);
        }
    }

    /**
     * There debug info contains the following items:
     * <ol>
     *     <li>request. (template, traceId, locale, timestamp)</li>
     *     <li>metadata</li>
     *     <li>trieTree</li>
     *     <li>queryBuilder, there could be multiple, use queryBuilder.1, queryBuilder.2 etc.</li>
     *     <li>fetchData, according to queryBuilder, there could be multiple items as well.</li>
     *     <li>dataTransform</li>
     *     <li>mustacheRender</li>
     * </ol>
     * For items except for `request`, each item is a map with 3 possible keys (executionTime, data, error)
     */
    public Map<String, Object> getDebugInfo() {
        return debugInfo;
    }

    public void addDebugInfo(String key, Object data) {
        if (debugMode) {
            MapUtils.setByDottedPath(this.debugInfo, key, data);
        }
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public <T> T runWithStopwatch(DebugInfoKey key, Supplier<T> supplier) {
        return runWithStopwatch(key, supplier, null);
    }

    /**
     * Use this method to collect intermediate states for troubleshooting.
     *
     * @param key can be dotted path.
     * @param supplier run your main logic here. Error in this function will be also captured.
     * @param transformer transform the data to put in the debug info.
     * @param <T>
     * @return
     */
    public <T> T runWithStopwatch(DebugInfoKey key, Supplier<T> supplier, Function<T, Object> transformer) {
        long begin = System.currentTimeMillis();
        String keyName = key.name();
        if (key.isWithMultipleRecords()) {
            int counter = stopwatchCounter.getOrDefault(key, 1);
            stopwatchCounter.put(key, counter + 1);
            keyName = keyName + "." + counter;
        }
        try {
            T result = supplier.get();
            // only call transformer in debug mode to avoid unnecessary cost
            if (this.isDebugMode()) {
                Object info = transformer == null ? result : transformer.apply(result);
                this.addDebugInfo(keyName + ".data", info);
            }
            return result;
        } catch (RuntimeException e) {
            this.addDebugInfo(keyName + ".error", e.getClass().getName() + ":" + e.getMessage());
            throw e;
        } finally {
            this.addDebugInfo(keyName + ".executionTime", System.currentTimeMillis() - begin);
        }
    }

    /**
     * <p>Context pattern when parsing the template. </p>
     *
     * @return default context object requires validation and with en_US system locale.
     */
    public static RenderContext getContext() {
        return contextCache.get();
    }

    public static <T> T runWithContext(RenderContext context, Supplier<T> supplier) {
        RenderContext oldContext = contextCache.get();
        try {
            contextCache.set(context);
            return supplier.get();
        } finally {
            contextCache.set(oldContext);
        }
    }

    public static void validate(RenderContext context) {
        Asserts.assertTrue(context != null, NotImplemented, "context argument is null.");
    }
}
