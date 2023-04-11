package me.smilingleo.services;

import static me.smilingleo.DebugInfoKey.dataTransform;
import static me.smilingleo.DebugInfoKey.mustacheRender;
import static java.lang.String.format;

import me.smilingleo.DebugInfoKey;
import me.smilingleo.RenderContext;
import me.smilingleo.functions.Lambdas;
import me.smilingleo.trie.DataTransformer;
import me.smilingleo.trie.TrieNode;
import me.smilingleo.utils.JsonUtils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Wig {

    private RenderContext context;

    public Wig(RenderContext context) {
        RenderContext.validate(context);
        this.context = context;
    }

    public Wig() {
        this.context = new RenderContext();
    }

    public String render(String template, Map<String, Object> data) {
        return RenderContext.runWithContext(context, () -> {
            List<TrieNode> roots = parseRootNodes(template);
            return doRender(roots, template, data);
        });
    }

    private String doRender(List<TrieNode> roots, String template, Map<String, Object> data) {
        DataTransformer transformer = new DataTransformer(context);
        Map<String, Object> transformed = context.runWithStopwatch(dataTransform,
                () -> {
                    Map<String, Object> map = transformer.transform(roots, data);
                    // put all lambdas
                    map.putAll(Lambdas.allLambdas());
                    // put all global variables
                    context.getVariables().entrySet().stream()
                            .filter(entry -> entry.getValue().isGlobal())
                            .forEach(entry -> map.putIfAbsent(entry.getKey(), entry.getValue().getEvaluatedValue()));
                    return map;
                },
                JsonUtils::uncheckedJsonPrettify);

        return context.runWithStopwatch(mustacheRender, () -> {
            MustacheFactory mustacheFactory = new DefaultMustacheFactory();
            Mustache mustache = mustacheFactory.compile(new StringReader(template), "template");
            StringWriter stringWriter = new StringWriter();
            try {
                mustache.execute(stringWriter, transformed).flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return stringWriter.getBuffer().toString();
        });
    }

    public static String renderDebugInfo(String templateHtml, Map<String, Object> debugInfo) {
        // transform the debug info, to turn queryBuilder and fetchData to List
        Map<String, Object> queryBuilder = (Map<String, Object>) debugInfo.getOrDefault(
                DebugInfoKey.queryBuilder.name(), new HashMap<>());
        List<Object> queryList = toList(queryBuilder);
        debugInfo.put(DebugInfoKey.queryBuilder.name(), queryList);

        Map<String, Object> fetchData = (Map<String, Object>) debugInfo.getOrDefault(DebugInfoKey.fetchData.name(),
                new HashMap<>());
        List<Object> rawDataList = toList(fetchData);
        debugInfo.put(DebugInfoKey.fetchData.name(), rawDataList);

        MustacheFactory factory = new DefaultMustacheFactory();

        try {
            Mustache mustache = factory.compile(new StringReader(templateHtml), "debug-template");
            StringWriter stringWriter = new StringWriter();
            mustache.execute(stringWriter, debugInfo).flush();
            return stringWriter.getBuffer().toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<Object> toList(Map<String, Object> map) {
        List<String> keyList = new LinkedList(map.keySet());
        Collections.sort(keyList);
        List<Object> list = new LinkedList<>();
        for (String key : keyList) {
            list.add(map.get(key));
        }
        return list;
    }

    private List<TrieNode> parseRootNodes(String template) {
        TrieTreeParser treeParser = new TrieTreeParser(context);
        List<TrieNode> roots = treeParser.parse(template);
        return roots;
    }
}
