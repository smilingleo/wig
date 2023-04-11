package me.smilingleo.commands;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.RenderContext;
import me.smilingleo.services.Wig;
import me.smilingleo.utils.StringUtils;
import me.smilingleo.utils.TestDataUtils;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ListToDictTest {

    @Test
    public void testListToDict() {
        String template = TestDataUtils.getFileContent("use-cases/localization.html");
        Map<String, Object> data = TestDataUtils.loadJsonAsMap("use-cases/localization.json");
        Wig wig = new Wig(new RenderContext());
        String rendered = wig.render(template, data);

        assertNotNull(rendered);
        List<String> lines = Stream.of(rendered.split("\\n"))
                .filter(line -> StringUtils.notNullOrBlank(line))
                .collect(toList());
        assertEquals(2, lines.size());
        assertEquals("账号: Leo Liu", lines.get(0).trim());
        assertEquals("账单编号: INV00000029", lines.get(1).trim());
    }
}
