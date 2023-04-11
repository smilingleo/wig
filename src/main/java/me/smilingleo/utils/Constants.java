package me.smilingleo.utils;

import me.smilingleo.decorators.FlatMap;
import me.smilingleo.decorators.Map;

import java.util.HashSet;
import java.util.Set;

public class Constants {

    public static final String EOL = "\n";
    public static final String DERIVED_LIST_KEY = "_Group";
    public static final String FILTER_OBJECT_PREFIX = "$";
    public static final String CMD_PREFIX = "Cmd_";
    public static final String FUNC_PREFIX = "Fn_";
    public static final String WRAPPER_PREFIX = "Wp_";
    public static final String CONNECTION_SUFFIX = "Connection";
    public static final String BY_ID_SUFFIX = "ById";
    public static final String CUSTOM_OBJECT_PREFIX = "default__";

    /**
     * This constant is used internally when building the GraphQL query string.
     */
    public static final String FILTER_PREFIX = "##";

    public static final Set<Class> SWITCH_CONTEXT_DECORATORS = getSwitchContextDecorators();

    private static Set<Class> getSwitchContextDecorators() {
        Set<Class> set = new HashSet<>();
        set.add(Map.class);
        set.add(FlatMap.class);
        return set;
    }

}
