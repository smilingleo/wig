package me.smilingleo.commands;

import me.smilingleo.RenderContext;
import me.smilingleo.services.Wig;
import me.smilingleo.utils.TestDataUtils;

import org.junit.Test;

import java.util.Map;

public class ComposeTest {

    @Test
    public void testCompose() {
        String template = TestDataUtils.getFileContent("use-cases/concat-list.html");
        Map<String, Object> data = TestDataUtils.loadJsonAsMap("use-cases/concat-list.json");
        Wig wig = new Wig(new RenderContext());
        System.out.println(wig.render(template, data));
    }
}
