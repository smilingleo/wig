package me.smilingleo.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListUtils {
    public static List trimTail(List list) {
        if (list == null) {
            return null;
        }
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.subList(0, list.size() - 1);
    }

    public static <T> T tail(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * Turn list like ["Invoice.Account","BillTo.FirstName"] into ["Invoice", "Account", "BillTo", "FirstName"].
     * @param dottedPaths a merge field list that could contain `dot`.
     * @return flattened path.
     */
    public static List<String> flatten(List<String> dottedPaths) {
        if (dottedPaths == null || dottedPaths.isEmpty()) {
            return Collections.emptyList();
        }
        return dottedPaths.stream()
                .flatMap(path -> StringUtils.dottedPathToList(path).stream())
                .collect(Collectors.toList());
    }

    public static boolean containIgnoreCase(Collection<String> collection, String key) {
        if (collection == null || collection.isEmpty()) {
            return false;
        }
        return collection.stream().anyMatch(item -> item.equalsIgnoreCase(key));
    }
}
