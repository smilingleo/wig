package me.smilingleo.services;

import static org.junit.Assert.assertNotNull;

import me.smilingleo.RenderContext;
import me.smilingleo.trie.TrieNode;
import me.smilingleo.utils.TestDataUtils;

import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * This test case is usually used for trouble shooting ad-hoc template.
 */
public class WigTest {
    @Test
    public void testRender() {
        RenderContext renderContext = new RenderContext();
        String template = TestDataUtils.getFileContent("sample-template.html");
        Wig service = new Wig(renderContext);
        Map<String, Object> inputData = TestDataUtils.loadJsonAsMap("sample-input.json");
        TrieTreeParser parser = new TrieTreeParser(renderContext);
        List<TrieNode> roots = parser.parse(template);
        roots.forEach(root -> System.out.println(root));

        String rendered = service.render(template, inputData);
        assertNotNull(rendered);
        System.out.println(rendered);
    }
}
