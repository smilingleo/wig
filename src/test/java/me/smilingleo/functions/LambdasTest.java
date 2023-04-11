package me.smilingleo.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import me.smilingleo.RenderContext;
import me.smilingleo.services.Wig;
import me.smilingleo.utils.StringUtils;

import org.junit.Test;

import java.util.HashMap;

public class LambdasTest {

    @Test
    public void unknownFieldAsBlank() {
        Wig wig = new Wig(new RenderContext());
        String rendered = wig.render("{{#Invoice}}{{#Wp_Eval}}"
                + "{{Unknown}}"
                + "{{/Wp_Eval}}{{/Invoice}}", new HashMap<String, Object>() {{
            put("Invoice", "invoice_id");
        }});
        assertTrue(StringUtils.isNullOrBlank(rendered));
    }

    @Test
    public void evalReturnBlankForBlankExpression() {
        assertEquals("", Lambdas.Eval.function().apply(""));
    }

}
