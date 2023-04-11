package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.exceptions.ValidationException;

import org.junit.Ignore;
import org.junit.Test;

public class FormatTest {

    @Test
    public void testParse() {
        Format format = Format.parse("Format(dd/MMM/yyyy)");
        assertNotNull(format);
        assertEquals("Format(dd/MMM/yyyy)", format.toString());

    }

    @Test(expected = ValidationException.class)
    public void testArgumentsForFormat() {
        Format format = Format.parse("Format()");
        assertNotNull(format);
        format = Format.parse("Format");
        assertNotNull(format);
    }

    @Test
    public void testEvaluateFormat_Date() {
        Format format = Format.parse("Format(dd/MMM/yyyy)");
        assertEquals("01/Mar/2021", format.evaluate("2021-03-01"));
        format = Format.parse("Format(dd/MMMM/YYYY)");
        assertEquals("01/March/2021", format.evaluate("2021-03-01"));
        format = Format.parse("Format(dd-MMMM-yyyy)");
        assertEquals("01-March-2021", format.evaluate("2021-03-01"));
        format = Format.parse("Format(dd-MM-yyyy)");
        assertEquals("01-03-2021", format.evaluate("2021-03-01"));
        format = Format.parse("Format(MMM,dd,yyyy)");
        assertEquals("Mar,01,2021", format.evaluate("2021-03-01"));
    }

    @Test(expected = ValidationException.class)
    public void testInvalidInputsForFormat() {
        Format format = Format.parse("Format(dd/MMM/yyyy)");
        assertEquals("01/Mar/2021", format.evaluate("123"));
        format = Format.parse("Format(dd/MMM/yyyy)");
        assertEquals("01/Mar/2021", format.evaluate("1234567890"));
        format = Format.parse("Format(dd/MMM/yyyy)");
        assertEquals("01/Mar/2021", format.evaluate("INV000000001"));
        //should throw exception when the format uses time fields but input is only a date field.
        format = Format.parse("Format(dd-MM-yy hh:mm:ss z)");
        assertEquals("01-03-21 12:00:00 UTC", format.evaluate("2021-03-01"));
    }

//     These test cases are already covered in core/src/test/resources/data-driven/decorators/test-cases.yml,
//     removed from here due to limitations with mocking and testing. But keeping the test case to see assert values for reference.
    @Ignore
    public void testEvaluateFormat_DateTime() {
        Format format = Format.parse("Format(dd MMM yyyy HH:mm:ss z)");
        assertEquals("19 Mar 2013 02:32:00 UTC", format.evaluate("2013-03-18T18:32:00-08:00"));
        format = Format.parse("Format(dd MMM yyyy)");
        assertEquals("18 Mar 2013", format.evaluate("2013-03-18T18:32:00+08:00"));
        format = Format.parse("Format(dd MMM yyyy HH:mm:ss z)");
        assertEquals("18 Mar 2013 13:02:00 UTC", format.evaluate("2013-03-18T18:32:00+05:30"));
        format = Format.parse("Format(dd MMM yyyy)");
        assertEquals("19 Mar 2013", format.evaluate("2013-03-18T18:32:00-05:30"));
        format = Format.parse("Format(dd-MMM-yyyy HH:mm:ss z)");
        assertEquals("23-Dec-2020 01:23:07 UTC", format.evaluate("2020-12-23T01:23:07.893Z"));
        format = Format.parse("Format(dd-MMM-yyyy HH:mm:ss z)");
        assertEquals("18-Mar-2013 01:02:03 UTC", format.evaluate("2013-03-18T01:02:03Z"));
        format = Format.parse("Format(dd,MMM-yyyy - HH/mm/ss : z)");
        assertEquals("18,Mar-2013 - 01/02/03 : UTC", format.evaluate("2013-03-18T01:02:03Z"));
    }

}
