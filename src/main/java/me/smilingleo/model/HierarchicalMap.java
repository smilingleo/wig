package me.smilingleo.model;

import me.smilingleo.utils.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HierarchicalMap<K, V> extends HashMap<K, V> {

    private HierarchicalMap parent;

    public HierarchicalMap() {
        super();
    }

    public HierarchicalMap(HierarchicalMap parent) {
        super();
        this.parent = parent;
    }

    @Override
    public V put(K key, V value) {
        setParentForHierarchicalMapValue(value);
        return super.put(key, value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        setParentForHierarchicalMapValue(value);
        return super.putIfAbsent(key, value);
    }

    private void setParentForHierarchicalMapValue(V value) {
        if (value instanceof HierarchicalMap) {
            ((HierarchicalMap) value).parent = this;
        }

        if (value instanceof List) {
            claimParent((List) value);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m == null) {
            return;
        }
        m.values().stream().forEach(value -> setParentForHierarchicalMapValue(value));

        super.putAll(m);
    }

    public HierarchicalMap getParent() {
        return parent;
    }

    public void claimParent(List list) {
        if (list == null) {
            return;
        }
        list.stream().filter(item -> item instanceof HierarchicalMap)
                .forEach(item -> ((HierarchicalMap) item).parent = this);
    }

    public static HierarchicalMap fromMap(Map mapData) {
        if (mapData == null) {
            return null;
        }

        if (mapData instanceof HierarchicalMap) {
            return (HierarchicalMap) mapData;
        }

        HierarchicalMap rtn = new HierarchicalMap();

        for (Entry entry : (Set<Entry>) mapData.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List) {
                List list = (List) value;
                List newList = (List) list.stream()
                        .map(item -> item instanceof Map ? fromMap((Map) item) : item)
                        .collect(Collectors.toList());
                rtn.put(key, newList);
            } else if (value instanceof Map) {
                Map subMap = (Map) value;
                HierarchicalMap subMap2 = fromMap(subMap);
                rtn.put(key, subMap2);
            } else {
                rtn.put(key, value);
            }
        }
        return rtn;
    }

    /**
     * This is to follow the Mustache spec, see https://github.com/mustache/spec/blob/master/specs/sections.yml#L62
     *
     * @param dottedPath e.g., Account.Name
     * @return The object contains that dotted path.
     */
    public HierarchicalMap findFieldOwner(String dottedPath) {
        HierarchicalMap cursor = this;
        do {
            if (MapUtils.containsDottedPath(cursor, dottedPath)) {
                return cursor;
            } else {
                if (cursor == cursor.getParent()) {
                    // break the loop, just in case.
                    return null;
                } else {
                    cursor = cursor.getParent();
                }
            }
        } while (cursor != null);
        return null;
    }

    public Object findValue(String dottedPath) {
        HierarchicalMap owner = findFieldOwner(dottedPath);
        return owner == null ? null : MapUtils.getByDottedPath(owner, dottedPath);
    }
}
