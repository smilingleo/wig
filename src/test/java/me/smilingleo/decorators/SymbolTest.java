package me.smilingleo.decorators;

import static me.smilingleo.decorators.MergeFieldParser.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import me.smilingleo.exceptions.ErrorCode;
import me.smilingleo.exceptions.ValidationException;

import org.junit.Test;

public class SymbolTest {

    @Test
    public void testParseSymbol() {

        Symbol.parse("Symbol");

        try {
            Symbol.parse("Symbol(");
            fail("Should throw ValidationException");
        } catch (Exception e) {
            assertTrue(e instanceof ValidationException);
            assertEquals(ErrorCode.InvalidFunctionArgument, ((ValidationException) e).getErrorCode());
        }
    }

    @Test
    public void testSymbol() {
        assertEquals("$", parse("Account.Currency|Symbol").dataBind("USD").get());
        assertEquals("¥", parse("Account.Currency|Symbol").dataBind("CNY").get());
        assertEquals(".د.ب", parse("Account.Currency|Symbol").dataBind("BHD").get());
        assertEquals(10, parse("Account.Balance|Symbol").dataBind(10).get());
    }
}
