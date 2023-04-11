package me.smilingleo.trie;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.emptySet;
import static java.util.stream.Stream.of;

import me.smilingleo.WithFieldArg;
import me.smilingleo.WithInputField;
import me.smilingleo.decorators.VariableField;
import me.smilingleo.utils.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p><a href="https://en.wikipedia.org/wiki/Trie">Trie</a> is a prefix tree, represent a path within a merge field
 * hierarchy.</p>
 *
 * <p>For example, for the following template:</p>
 *
 * <pre>
 *     {{#Invoice}}
 *          {{#InvoiceItems}}
 *              {{ChargeName}}
 *          {{/InvoiceItems}}
 *     {{/Invoice}}
 * </pre>
 *
 * <p>There is a path Invoice -> InvoiceItems -> ChargeName, which is a branch of a TrieTree.</p>
 *
 * @author leo liu
 */
public class TrieNode {

    private WithInputField inputField;
    private List<String> metaDataPath;
    private TrieNode parent;
    private List<TrieNode> children;

    public TrieNode(WithInputField inputField, List<String> metaDataPath, TrieNode parent) {
        this.inputField = inputField;
        this.metaDataPath = metaDataPath;
        this.parent = parent;
        this.children = new LinkedList<>();

        if (parent != null) {
            parent.children.add(this);
        }
    }

    public WithInputField getInputField() {
        return inputField;
    }

    public String getLabel() {
        return inputField.toString();
    }

    public TrieNode getParent() {
        return parent;
    }

    public List<TrieNode> getChildren() {
        return children;
    }

    public List<TrieNode> pathFromRoot() {
        List<TrieNode> path = new LinkedList<>();
        TrieNode node = this;
        while (node != null) {
            path.add(0, node);
            node = node.parent;
        }
        return path;
    }

    /**
     * <p>This method return the branches in the raw data result. Used in transformation phase, how to fetch data from
     * raw result.</p>
     * <p>This path does NOT include field names in the decorators.</p>
     *
     * @return the branch in the meta tree, from root to this node.
     */
    public List<String> inputObjectMetaPath() {
        return new LinkedList<>(metaDataPath);
    }


    /**
     * find the root node of any trie node of a tree.
     *
     * @return root node.
     */
    public TrieNode root() {
        TrieNode cursor = this;
        while (cursor.parent != null) {
            cursor = cursor.parent;
        }
        return cursor;
    }

    /**
     * Return all nodes in the Trie Tree.
     *
     * @return set of nodes.
     */
    public Set<TrieNode> allNodes() {
        Set<TrieNode> nodes = new HashSet<>();
        traverseForAllNodes(this.root(), nodes);
        return nodes;
    }

    private void traverseForAllNodes(TrieNode node, Set<TrieNode> nodes) {
        nodes.add(node);
        for (TrieNode child : node.children) {
            traverseForAllNodes(child, nodes);
        }
    }

    /**
     * Tell if a node is an effective leaf node.
     *
     * @return true if it's leaf.
     */
    public boolean isLeaf() {
        if (children.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * <p>Return all data branches. Each branch is a dotted path from root to the leaf node.</p>
     *
     * @return set of dotted path.
     */
    public Set<String> allTemplateBranches() {
        Set<TrieNode> allNodes = this.allNodes();
        final Set<String> branches = allNodes.stream()
                .flatMap(node -> node.ownTemplateBranches().stream())
                .filter(StringUtils::notNullOrBlank)
                .collect(Collectors.toSet());

        return branches.stream()
                // if there is a longer path which contains the whole path of current branch, ignore it.
                .filter(branch -> !branches.stream()
                        .anyMatch(item -> !item.equals(branch) && item.startsWith(branch + ".")))
                .collect(Collectors.toSet());
    }

    public Set<String> ownTemplateBranches() {
        if (this.inputObjectMetaPath().isEmpty()) {
            return emptySet();
        }
        // For variable field, the data path is handled by the Assign command field,
        // and it's the Assign command field that is under the right context to build a data path.
        // The place where VariableField is used is often not the right context.
        // for example: {{#Invoice}} {{Cmd_Assign(VarSymbol,Account.Currency|Symbol)}}
        // {{#InvoiceItems}} {{VarSymbol}}{{ChargeAmount}} {{/InvoiceItems}}{{/Invoice}}
        // Account.Currency is handled by Assign command and generate Invoice.Account.Currency path
        // If we substitute VarSymbol with real field, we will get `Invoice.InvoiceItems.Account.Currency`,
        // it might not be a big problem, but sometimes it matters,
        // see `Previous invoices` test case in `inline-discounts/test-cases.yml`
        if (this.getInputField() instanceof VariableField) {
            return emptySet();
        }

        List<String> argFieldNames = null;
        if (this.inputField instanceof WithFieldArg) {
            argFieldNames = ((WithFieldArg) this.inputField).getArgFieldNames();
        } else {
            argFieldNames = Collections.emptyList();
        }
        String parentPath = this.pathFromRoot().stream()
                .map(item -> String.join(".", item.inputObjectMetaPath()))
                .filter(StringUtils::notNullOrBlank)
                .collect(Collectors.joining("."));
        Stream<String> paths = argFieldNames.isEmpty() ? of(join(".", this.inputObjectMetaPath()))
                : argFieldNames.stream()
                        .map(fieldName -> parentPath.isEmpty() ? fieldName : parentPath + "." + fieldName);
        return paths.filter(StringUtils::notNullOrBlank).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        TrieNode parent = this.parent;
        int depth = 1;
        while (parent != null) {
            depth++;
            parent = parent.parent;
        }
        String childrenStr = children.stream().map(child -> child.toString()).collect(Collectors.joining(""));
        String indent = "\n" + join("", Collections.nCopies(depth - 1, "\t"));
        return format("%sName: %s, MetaPath: [%s], IsLeaf: %s %s",
                indent, this.inputField, join("->", this.inputObjectMetaPath()), this.isLeaf(), childrenStr);
    }
}
