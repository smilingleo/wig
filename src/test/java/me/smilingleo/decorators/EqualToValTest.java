package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EqualToValTest {

    @Test
    public void testParse() {
        EqualToVal equalToVal = EqualToVal.parse("EqualToVal(20.0)");
        assertNotNull(equalToVal);
        assertEquals("EqualToVal(20.0)", equalToVal.toString());

    }

    @Test
    public void testValuesWithZeros() {
        EqualToVal equalToVal = EqualToVal.parse("EqualToVal(20.0)");
        assertTrue("should be same", equalToVal.evaluate("20.0"));
        assertTrue("should be same", equalToVal.evaluate("20"));
        assertTrue("should be same", equalToVal.evaluate("20.00"));

        EqualToVal equalToVal1 = EqualToVal.parse("EqualToVal(21.2)");
        assertTrue("should be same", equalToVal1.evaluate("21.20"));
        assertTrue("should be same", equalToVal1.evaluate("21.2"));

        EqualToVal equalToVal2 = EqualToVal.parse("EqualToVal(0)");
        assertTrue("should be same", equalToVal2.evaluate("0.0"));
        assertTrue("should be same", equalToVal2.evaluate("0.00"));


    }
}
