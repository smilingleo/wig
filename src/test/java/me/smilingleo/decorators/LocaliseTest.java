package me.smilingleo.decorators;

import static me.smilingleo.decorators.MergeFieldParser.parse;
import static org.junit.Assert.assertEquals;

import me.smilingleo.RenderContext;

import org.junit.Test;

import java.util.HashMap;

public class LocaliseTest {
    @Test
    public void testNoArgument() {
        RenderContext cnContext = new RenderContext();

        Localise decorator = Localise.parse("Localise");
        assertEquals("en_US", RenderContext.runWithContext(cnContext, () -> decorator.getLocale()));

        cnContext.setLocale("zh_CN");
        assertEquals("zh_CN", RenderContext.runWithContext(cnContext, () -> decorator.getLocale()));

        assertEquals("Localise", decorator.toString());
    }

    @Test
    public void testLocalise() {
        java.util.Map<String, Object> data = new HashMap<String, Object>() {{
            put("Number", 1234.56);
            put("Date", "2021-06-07");
            put("DateTime", "2021-06-07T01:02:03");
            put("DateTimeWithOffset", "2021-06-07T01:02:03+08:00");
            put("DateTimeInUTC", "2021-06-07T01:02:03Z");
            put("Name", "unknown");
        }};

        assertEquals("1.234,56",
                parse("Number|Localise(it_IT)").dataBind(data.get("Number")).get());
        assertEquals("07/06/2021",
                parse("Date|Localise(it_IT)").dataBind(data.get("Date")).get());
        assertEquals("07/06/2021 01:02:03",
                parse("DateTime|Localise(it_IT)").dataBind(data.get("DateTime")).get());
        assertEquals("07/06/2021 01:02:03Z",
                parse("DateTimeInUTC|Localise(it_IT)").dataBind(data.get("DateTimeInUTC")).get());
        assertEquals("07/06/2021 01:02:03+08:00",
                parse("DateTimeWithOffset|Localise(it_IT)").dataBind(data.get("DateTimeWithOffset")).get());
        assertEquals("unknown", parse("Name|Localise").dataBind(data.get("Name")).get());
    }
}
