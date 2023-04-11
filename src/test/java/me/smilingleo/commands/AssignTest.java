package me.smilingleo.commands;

import me.smilingleo.RenderContext;
import me.smilingleo.services.Wig;
import me.smilingleo.utils.TestDataUtils;

import org.junit.Test;

import java.util.Map;

public class AssignTest {

    @Test
    public void testAssign() {
        String template = TestDataUtils.getFileContent("use-cases/inline-discount.html");
        Map<String, Object> data = TestDataUtils.loadJsonAsMap("use-cases/inline-discount.json");

        Wig wig = new Wig(new RenderContext());
        System.out.println(wig.render(template, data));
    }
}
