package me.smilingleo.functions;

import static org.junit.Assert.assertEquals;

import me.smilingleo.RenderContext;
import me.smilingleo.services.Wig;
import me.smilingleo.utils.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

public class TodayTest {

    @Test
    public void testToday() {
        RenderContext renderContext = new RenderContext();
        renderContext.setTimeZone("America/Los_Angeles");
        String template = "{{Invoice.Id}}\n{{Fn_Today()}}\n{{Fn_Today()|Localise(en_US)}}\n" +
                "{{#Invoice.Account.Invoices|FilterByRef(InvoiceDate,LT,Fn_Today())}}\n" +
                "{{InvoiceDate}}\n" +
                "{{/Invoice.Account.Invoices|FilterByRef(InvoiceDate,LT,Fn_Today())}}";
        String rawDataStr = "{\n"
                + "      \"Invoice\": {\n"
                + "        \"Id\": \"2c92c8fc7bdd0f5a017bdf7d7c2630a7\",\n"
                + "        \"Account\": {\n"
                + "          \"Invoices\": [\n"
                + "                {\n"
                + "                    \"Id\": \"2c92c8fc7bdd0f5a017bdf7d7c2630a7\",\n"
                + "                    \"InvoiceDate\": \"2021-03-02\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"Id\": \"2c92c8fc7bdd0f5a017bdf7d7c2630a8\",\n"
                + "                    \"InvoiceDate\": \"2099-03-03\"\n"
                + "                }\n"
                + "          ]\n"
                + "        }\n"
                + "      }\n"
                + "  }";
        Map<String, Object> rawData = JsonUtils.uncheckedStringToJson(rawDataStr,
                new TypeReference<Map<String, Object>>() {});
        Wig wig = new Wig(renderContext);
        String rendered = wig.render(template, rawData);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        df1.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String today1 = df1.format(calendar.getTime());

        SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
        df2.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String today2 = df2.format(calendar.getTime());
        String expected = String.format("%s\n%s\n%s\n%s\n", "2c92c8fc7bdd0f5a017bdf7d7c2630a7", today1, today2,
                "2021-03-02");

        assertEquals(expected, rendered);
    }
}
