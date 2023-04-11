package me.smilingleo.utils;

import static me.smilingleo.utils.Asserts.assertNotNull;
import static me.smilingleo.utils.Asserts.assertTrue;
import static java.lang.String.format;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.model.HierarchicalMap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MapUtils {

    /**
     * Access value by dotted path, like `Account.BillTo.FirstName`, doesn't support list in the middle, for example
     * `Account.Invoices.InvoiceNumber`.
     */
    public static Object getValueByDottedPath(Map<String, Object> data, String dottedPath) {
        List result = simpleJsonPath(data, dottedPath);
        switch (result.size()) {
            case 0:
                return null;
            case 1:
                return result.get(0);
            default:
                throw new ValidationException(ErrorCode.InvalidDataPath, "Don't use getValueByDottedPath to fetch list");
        }
    }

    /**
     * Fetch data by dotted path.
     */
    public static List simpleJsonPath(Map<String, Object> data, String dottedPath) {
        List result = new LinkedList();
        // Note: Don't replace `dottedPath.split("\\.")` with `StringUtils.dottedPathToArray(dottedPath)`.
        // Since we have to strictly split the string by `.` in this class.
        // If the data is a Map, Mustache will interpret `A.B` as a Map of Map,
        // (A is a key, the value is a map with key B).
        jsonPath(data, dottedPath.split("\\."), result);
        return result;
    }

    public static List simpleJsonPath(Map<String, Object> data, List<String> path) {
        List result = new LinkedList();
        List<String> newPath = path.stream()
                .flatMap(str -> {
                    // by Mustache dot notation spec.
                    return Stream.of(str.split("\\."));
                })
                .collect(Collectors.toList());
        jsonPath(data, newPath.toArray(new String[]{}), result);
        return result;
    }

    private static void jsonPath(Object data, String[] path, List result) {
        if (path != null && path.length > 0) {
            // data must be an array
            Map<String, Object> node = (Map<String, Object>) data;
            String key = path[0];
            if (node.containsKey(key)) {
                Object nextNode = node.get(key);

                String[] leftPath = new String[path.length - 1];
                System.arraycopy(path, 1, leftPath, 0, path.length - 1);
                if (nextNode == null) {
                    return;
                }
                if (nextNode instanceof Map) {
                    jsonPath((Map<String, Object>) nextNode, leftPath, result);
                } else if (nextNode instanceof List) {
                    List nextList = (List) nextNode;
                    nextList.forEach(item -> jsonPath(item, leftPath, result));
                } else {
                    // nextNode is null or not list nor map, it must be value type
                    jsonPath(nextNode, null, result);
                }
            }
        } else {
            result.add(data);
        }
    }

    public static Map<String, Object> fromTuples(Tuple... rows) {
        Map<String, Object> rtn = new HashMap<>();
        for (Tuple row : rows) {
            rtn.put(row.getLeft(), row.getRight());
        }
        return rtn;
    }

    public static boolean containsDottedPath(Map<String, Object> data, String dottedPath) {
        if (data == null) {
            return false;
        }

        if (".".equals(dottedPath)) {
            return true;
        }
        String[] path = dottedPath.split("\\.");
        Map<String, Object> cursor = data;
        for (int i = 0; i < path.length; i++) {
            if (cursor == null || !cursor.containsKey(path[i])) {
                return false; // early return for performance
            }

            if (i == path.length - 1) {
                return cursor.containsKey(path[i]);
            }

            if (cursor.get(path[i]) instanceof Map) {
                cursor = (Map<String, Object>) cursor.get(path[i]);
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * <p>A helper method to implement map.get("a").get("b") for convenience.</p>
     */
    public static Object getByDottedPath(Map<String, Object> data, String dottedPath) {
        if (data == null) {
            return null;
        }
        if (".".equals(dottedPath)) {
            return data;
        }
        assertNotNull(dottedPath, "Can't fetch data via a null key.");
        Object value = null;
        Map<String, Object> cursor = data;
        String[] parts = dottedPath.split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            value = cursor.get(parts[i]);
            if (value == null) {
                return null;
            }
            // all non-tail object MUST be Map
            boolean nonTailElementIsMap = value instanceof Map;
            assertTrue(nonTailElementIsMap, ErrorCode.InvalidExpression,
                    format("Wrong parameter, expect a Map type data but we received %s for key %s",
                            value.getClass().getSimpleName(), parts[i]));
            cursor = (Map<String, Object>) value;
        }
        return cursor == null ? null : cursor.get(parts[parts.length - 1]);
    }

    /**
     * <p>helper method to implement something like:</p>
     * <pre>
     *     Map data;
     *     Map aMap = data.get("a");
     *     Map bMap = aMap.get("b");
     *     Object value;
     *     bMap.set("c", value);
     *     // instead, we can do:
     *     setByDottedPath(data, "a.b.c", value);
     * </pre>
     *
     * @param dottedPath if part of the path is absent, this method will create an empty map for it.
     */
    public static void setByDottedPath(Map<String, Object> data, String dottedPath, Object value) {
        if (data == null) {
            return;
        }
        assertNotNull(dottedPath, "Can't set data with a null key.");
        Map<String, Object> cursor = data;

        String[] parts = dottedPath.split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            cursor.putIfAbsent(parts[i],
                    cursor instanceof HierarchicalMap ? new HierarchicalMap((HierarchicalMap) cursor)
                            : new HashMap<>());
            Object child = cursor.get(parts[i]);
            // all non-tail object MUST be Map
            boolean nonTailElementIsMap = child instanceof Map;
            assertTrue(nonTailElementIsMap, ErrorCode.InvalidExpression,
                    format("Wrong parameter, expect a Map type data but we received %s for key %s",
                            child.getClass().getSimpleName(), parts[i]));

            cursor = (Map<String, Object>) child;
        }
        cursor.putIfAbsent(parts[parts.length - 1], value);
    }


    public static <T> T getIgnoreCase(Map<String, T> data, String keyIgnoreCase) {
        return data.keySet()
                .stream()
                .filter(key -> key.equalsIgnoreCase(keyIgnoreCase))
                .findFirst()
                .map(key -> data.get(key))
                .orElse(null);
    }

    public static String getFieldName(String codeName) {
        int pipePos = codeName.indexOf('|');
        return pipePos < 0 ? codeName : codeName.substring(0, pipePos);
    }
}
