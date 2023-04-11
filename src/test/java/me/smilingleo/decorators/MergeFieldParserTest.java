package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MergeFieldParserTest {
    @Test
    public void testParse() {
        String label = "InvoiceItems|SortBy(ChargeAmount,DESC)|GroupBy(ChargeName)";
        MergeField mergeField = MergeFieldParser.parse(label);
        assertNotNull(mergeField);
        assertEquals("InvoiceItems", mergeField.getInputFieldName());
        assertTrue(mergeField.getDecorators().isPresent());
        assertEquals(2, mergeField.getDecorators().get().size());
    }

    @Test
    public void testParseLocalise() {
        String label = "Quantity|Localise";
        MergeField mergeField = MergeFieldParser.parse(label);
        assertNotNull(mergeField);
        assertEquals("Quantity", mergeField.getInputFieldName());
        assertTrue(mergeField.getDecorators().isPresent());
        assertEquals(1, mergeField.getDecorators().get().size());
        assertTrue(mergeField.getDecorators().get().get(0) instanceof Localise);
    }

    @Test
    public void testParseSymbol() {
        String label = "Currency|Symbol";
        MergeField mergeField = MergeFieldParser.parse(label);
        assertNotNull(mergeField);
        assertEquals("Currency", mergeField.getInputFieldName());
        assertTrue(mergeField.getDecorators().isPresent());
        assertEquals(1, mergeField.getDecorators().get().size());
        assertTrue(mergeField.getDecorators().get().get(0) instanceof Symbol);
    }
}
