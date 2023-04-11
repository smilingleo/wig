package me.smilingleo.services;

import me.smilingleo.RenderContext;
import me.smilingleo.trie.TrieNode;
import me.smilingleo.utils.TestDataUtils;

import org.junit.Test;

import java.util.List;

public class TrieTreeParserTest {

    @Test
    public void testParse() {
        String template = TestDataUtils.getFileContent("sample-template.html");
        List<TrieNode> roots = new TrieTreeParser(new RenderContext()).parse(template);
        roots.forEach(root -> System.out.println(root));
    }

}
