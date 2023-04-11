package me.smilingleo.trie;

import static me.smilingleo.utils.StringUtils.dottedPathToList;
import static java.util.Arrays.asList;

import me.smilingleo.RenderContext;
import me.smilingleo.WithInputField;
import me.smilingleo.commands.Command;
import me.smilingleo.decorators.Decorator;
import me.smilingleo.decorators.FilterByRef;
import me.smilingleo.decorators.MergeField;
import me.smilingleo.decorators.ReturningScalar;
import me.smilingleo.decorators.VariableField;
import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.functions.Function;
import me.smilingleo.model.HierarchicalMap;
import me.smilingleo.utils.ListUtils;
import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.StringUtils;

import me.smilingleo.utils.Constants;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Transform the raw data result via ObjectQuery to template specified shape.</p>
 */
public class DataTransformer {

    private RenderContext context;

    public DataTransformer(RenderContext context) {
        RenderContext.validate(context);
        this.context = context;
    }

    /**
     * @param rawData the `data` element in the GraphQL response.
     */
    public Map<String, Object> transform(List<TrieNode> roots, Map<String, Object> rawData) {
        // in case any decorator functions need to leverage RenderContext.getContext().getMetadata()
        // during data transformation phase.
        return RenderContext.runWithContext(context, () -> {
            // change the map to a HierarchicalMap that is parent-aware,
            // so that in case of an attribute is not available in context object
            // we can try to find it in its parent object. See Mustache spec for details.
            HierarchicalMap hierarchicalMap = HierarchicalMap.fromMap(rawData);

            for (TrieNode root : roots) {
                transformNode(root, hierarchicalMap);
            }
            return hierarchicalMap;
        });
    }

    /**
     * Recursively
     * <ul>
     *     <li>apply decorators</li>
     *     <li>execute command</li>
     *     <li>execute function</li>
     * </ul>
     *
     * @param node each node represent a merge field in the template.
     * @param rawData the data being manipulated during the transformation.
     */
    private void transformNode(TrieNode node, Map<String, Object> rawData) {
        WithInputField field = node.getInputField();
        String fieldName = field.getInputFieldName();

        // determine data path in the dict.
        // if parent is null, the input is one of the root objects
        List<String> transformedParentPath =
                node.getParent() == null ? ListUtils.trimTail(dottedPathToList(fieldName))
                        : getParentTransformedPath(node);

        // holder is the node where the transformed children data nodes being appended.
        // in format/structure of the merge field.
        List<Map<String, Object>> holder = findHolder(rawData, transformedParentPath);

        // in case parent is empty, no need to process its children.
        if (holder.isEmpty()) {
            return;
        }

        if (!(field instanceof VariableField) && !fieldName.startsWith(Constants.WRAPPER_PREFIX)) {
            doTransformation(node, field, fieldName, transformedParentPath, holder);
        }

        for (TrieNode child : node.getChildren()) {
            transformNode(child, rawData);
        }
    }

    private void doTransformation(TrieNode node, WithInputField field, String fieldName, List<String> transformedParentPath,
            List<Map<String, Object>> holder) {
        // the nodeKey is the branch to grow based on the holder object.
        String nodeKey = getKeyForNode(node, transformedParentPath);

        if (field instanceof Command) {
            Command cmd = (Command) field;
            for (Map<String, Object> object : holder) {
                cmd.execute(object);
            }
        } else if (field instanceof Function) {
            Function fn = (Function) field;
            for (Map<String, Object> object : holder) {
                Object result = fn.evaluate(object);
                MapUtils.setByDottedPath(object, nodeKey, result);
            }
        } else if (field instanceof MergeField) {
            MergeField mergeField = (MergeField) field;
            List<Decorator> decorators = mergeField.getDecorators().orElse(Collections.emptyList());
            boolean hasRefDecorator = decorators.stream()
                    .anyMatch(decorator -> decorator instanceof FilterByRef);
            for (Map<String, Object> object : holder) {

                Object data = getDataFromContext(object, fieldName);
                // if the `data` is null, we still need to apply the decorators because some decorators might change the initial value
                // for example, `|Default(0)`
                List<HierarchicalMap> originalParents = null;
                Object originalData = data;
                try {
                    if (hasRefDecorator) {
                        // establish parent relationship so that the FilterByRef can find via parent path.
                        if (object instanceof HierarchicalMap && data instanceof List) {
                            originalParents = (List<HierarchicalMap>) ((List) data).stream()
                                    .filter(item -> item instanceof HierarchicalMap)
                                    .map(item -> ((HierarchicalMap) item).getParent())
                                    .collect(Collectors.toList());
                            ((HierarchicalMap<String, Object>) object).claimParent((List) data);
                        }
                    }
                    for (Decorator decorator : decorators) {
                        data = decorator.evaluate(data);
                    }

                    // if data is null, leave it to the mustache engine to get from outer parent.
                    // otherwise, we will explicitly add a null property to the holder object, which break the out fetching attemption by mustache.
                    if (data != null) {
                        MapUtils.setByDottedPath(object, nodeKey, data);
                    }
                } finally {
                    // restore the original parent relationship, which is potentially used by getDataFromContext
                    // put in try finally clause to prevent unexpected exception.
                    if (originalParents != null) {
                        Iterator<HierarchicalMap> parentOter = originalParents.iterator();
                        ((List) originalData).stream()
                                .filter(item -> item instanceof HierarchicalMap)
                                .forEachOrdered(item -> {
                                    parentOter.next().claimParent(asList(item));
                                });
                    }
                }

            }
        } else {
            throw new ValidationException(ErrorCode.UnknownField, "Unknown field type" + field);
        }
    }

    /**
     * Given a node, by the transformedParentPath, we can find the holder object, and based on that holder object, we
     * need to append data, aka, to "grow" the tree. The key is the path to grow.
     */
    private String getKeyForNode(TrieNode node, List<String> transformedParentPath) {
        WithInputField field = node.getInputField();
        String parentPath = String.join(".", transformedParentPath);
        return node.getParent() == null ? field.toString().startsWith(parentPath + ".")
                ? field.toString().substring(parentPath.length() + 1) : StringUtils.tailOfDottedPath(field.toString())
                : field.toString();

    }

    // We only need the node path to find the holder objects, no?
    private List<Map<String, Object>> findHolder(Map<String, Object> rawData, List<String> transformedParentPath) {
        if (transformedParentPath == null) {
            return asList(rawData);
        }

        List<String> parentPath = transformedParentPath;

        List<Map<String, Object>> holder = MapUtils.simpleJsonPath(rawData, parentPath);
        if (holder.isEmpty()) {
            return holder;
        }

        do {
            Object oneValue = holder.iterator().next();
            boolean isObjectType = oneValue instanceof List || oneValue instanceof Map;
            // to handle Mustache conditional section
            if (!isObjectType) {
                // in case holder holds a boolean, use its parent path.
                parentPath.remove(parentPath.size() - 1);
                holder = MapUtils.simpleJsonPath(rawData, parentPath);
            } else {
                break;
            }
        } while (!parentPath.isEmpty());

        return holder;
    }

    private List<String> getParentTransformedPath(TrieNode node) {
        List<TrieNode> pathFromRoot = node.getParent().pathFromRoot();
        // in case the `Invoice.InvoiceItems|IsEmpty.InvoiceItems`,
        // remove the conditional section `InvoiceItems|IsEmpty`.
        for (int i = 0; i < pathFromRoot.size() - 1; i++) {
            TrieNode item = pathFromRoot.get(i);
            WithInputField field = item.getInputField();
            if (field instanceof Command) {
                continue;
            }
            MergeField mergeField = (MergeField) field;
            boolean lastIsAggregator = mergeField.getDecorators()
                    .map(decorators -> decorators.get(decorators.size() - 1) instanceof ReturningScalar)
                    .orElse(false);
            if (lastIsAggregator) {
                pathFromRoot.remove(item);
                i++;
            }
        }
        return pathFromRoot.stream()
                .filter(n -> n.getInputField() instanceof MergeField)
                .filter(n -> !n.getLabel().startsWith(Constants.WRAPPER_PREFIX))
                .map(n -> n.getInputField().toString())
                .collect(Collectors.toList());
    }

    private Object getDataFromContext(Map<String, Object> object, String fieldName) {
        Map<String, Object> cursor = object;
        Object value = MapUtils.getByDottedPath(cursor, fieldName);
        int maxDepth = 1000;
        while (value == null && cursor instanceof HierarchicalMap) {
            // try parent
            HierarchicalMap parent = ((HierarchicalMap) cursor).getParent();
            if (cursor == parent) {
                throw new IllegalStateException("circular dependency detected.");
            }
            if (maxDepth-- < 0) {
                throw new IllegalStateException("too many levels of nested fields.");
            }
            cursor = parent;
            value = MapUtils.getByDottedPath(cursor, fieldName);
        }
        return value;
    }

}
