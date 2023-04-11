package me.smilingleo.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class StringUtilsTest {

    @Test
    public void testUnquote() {
        assertEquals("Hello", StringUtils.unquote("'Hello'"));
        assertEquals("Hello", StringUtils.unquote(" 'Hello' "));
        assertEquals("Hello", StringUtils.unquote(" 'Hello'"));
        assertEquals("Hello", StringUtils.unquote("\"Hello\""));
        assertEquals("\"Hello", StringUtils.unquote("\"Hello"));
        assertEquals("Hello'", StringUtils.unquote("Hello'"));
        assertEquals("", StringUtils.unquote(null));
    }

    @Test
    public void testIsText() {
        assertTrue(StringUtils.isText("'value'"));
        assertTrue(StringUtils.isText("\"value\""));
        assertFalse(StringUtils.isText("\"value"));
        assertFalse(StringUtils.isText("value"));
        assertFalse(StringUtils.isText("value'"));
    }

    @Test
    public void testTailOfDottedPath() {
        assertEquals("B.C", StringUtils.tailOfDottedPath("A|FilterByValue(a.b.c,EQ,0).B.C"));
        assertEquals("A|FilterByValue(a.b.c,EQ,0)", StringUtils.tailOfDottedPath("A|FilterByValue(a.b.c,EQ,0)"));
    }

    @Test
    public void testHeadOfDottedPath() {
        assertEquals("A", StringUtils.headOfDottedPath("A.A|FilterByValue(a.b.c,EQ,0).B.C"));
        assertEquals("A|FilterByValue(a.b.c,EQ,0)", StringUtils.headOfDottedPath("A|FilterByValue(a.b.c,EQ,0).B.C"));
    }

    @Test
    public void testToList() {
        assertEquals(1, StringUtils.dottedPathToList("A").size());
        assertEquals(3, StringUtils.dottedPathToList("A.B.C").size());
        List<String> list = StringUtils.dottedPathToList("A.B|Func(Obj.Att,Arg).C");
        assertEquals(3, list.size());
        assertEquals("B|Func(Obj.Att,Arg)", list.get(1));
    }

    @Test
    public void testParseArguments() {
        assertTrue("no argument returns empty list", StringUtils.parseFunctionArguments("").isEmpty());
        assertEquals(3, StringUtils.parseFunctionArguments("FilterByValue(ProcessingType,EQ,0)").size());

        String arguments = "Fn_Calc(Invoice.InvoiceItems|FilterByValue(ChargeAmount,EQ,'he\"llo,world')|Size,Add,10)";
        List<String> list = StringUtils.parseFunctionArguments(arguments);
        assertEquals(3, list.size());
        assertEquals("Invoice.InvoiceItems|FilterByValue(ChargeAmount,EQ,'he\"llo,world')|Size", list.get(0));
        assertEquals("Add", list.get(1));
        assertEquals("10", list.get(2));

    }
}
