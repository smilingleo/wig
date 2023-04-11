package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.exceptions.ValidationException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RoundTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testDecorator() {
        Round round = Round.parse("Round(0)");
        assertNotNull(round);
        assertEquals(null, round.evaluate(""));
        assertEquals(null, round.evaluate("   "));
        assertEquals(null, round.evaluate(null));
        assertEquals("0", round.evaluate("0"));
        assertEquals("10", round.evaluate("10"));

        round = Round.parse("Round(1)");
        assertNotNull(round);
        assertEquals(null, round.evaluate(""));
        assertEquals(null, round.evaluate("   "));
        assertEquals(null, round.evaluate(null));
        assertEquals("0.0", round.evaluate("0"));
        assertEquals("10.0", round.evaluate("10"));

        round = Round.parse("Round(2)");
        assertNotNull(round);
        assertEquals(null, round.evaluate(""));
        assertEquals(null, round.evaluate("   "));
        assertEquals(null, round.evaluate(null));
        assertEquals("0.00", round.evaluate("0"));
        assertEquals("10.00", round.evaluate("10"));
    }

    @Test
    public void testFailedToRoundIfInputIsNonNumericValue() {

        expectedEx.expect(ValidationException.class);
        expectedEx.expectMessage("Function Round can only be used to a numeric input field.");

        Round round = Round.parse("Round(2)");
        assertNotNull(round);
        assertEquals(null, round.evaluate("test"));
        assertEquals(null, round.evaluate("123A"));
        assertEquals(null, round.evaluate("123-1"));
        assertEquals(null, round.evaluate("@#$%!*&^"));
    }

}