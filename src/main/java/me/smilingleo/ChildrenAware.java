package me.smilingleo;

/**
 * <p>In most case, each function is self-contained, can be independently executed.</p>
 *
 * <p>But there are use cases where a command needs to be meaningful together with its children,
 * for example, Compose, this command needs to know the columns and argument lists.</p>
 *
 * <p>When parsing the TrieNode, the parser will check if a node is children aware,
 * if so, all its immediate children will be added to it.</p>
 */
public interface ChildrenAware {

    void addChild(WithInputField inputField);
}
