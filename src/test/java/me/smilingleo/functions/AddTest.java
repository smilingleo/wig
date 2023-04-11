package me.smilingleo.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.model.HierarchicalMap;

import org.junit.Test;

import java.util.HashMap;

public class AddTest {

    @Test
    public void testParse() {
        Add fnAdd = Add.parse("Fn_Add(Account.Name,'_')");
        assertNotNull(fnAdd);
        assertNotNull(Add.parse("Fn_Add(Invoice.Balance,10)"));
        assertNotNull(Add.parse("Fn_Add(Invoice.Balance,Invoice.Amount)"));
    }

    @Test(expected = ValidationException.class)
    public void shouldNotTakeOnlyOneArgument() {
        Add.parse("Fn_Add(Invoice.Balance)");
    }

    @Test(expected = ValidationException.class)
    public void shouldNotTakeMoreThanTwoArguments() {
        Add.parse("Fn_Add(Invoice.Balance,Invoice.Amount,10)");
    }

    @Test
    public void testEvaluateTwoMergeFields() {
        HierarchicalMap context = HierarchicalMap.fromMap(new HashMap() {{
            put("Invoice", new HashMap() {{
                put("Amount", 100.00);
                put("Balance", 100.00);
                put("Number", "INV-00001");
            }});
        }});

        Add fnAdd = Add.parse("Fn_Add(Invoice.Balance,Invoice.Amount)");
        assertEquals(200.00, fnAdd.evaluate(context));
        assertEquals(150.00, Add.parse("Fn_Add(Invoice.Balance,50)").evaluate(context));
        // if a field is absent, return left side.
        assertEquals(100.00, Add.parse("Fn_Add(Invoice.Balance,NullField)").evaluate(context));
        assertEquals("INV-00001-001", Add.parse("Fn_Add(Invoice.Number,'-001')").evaluate(context));
    }
}
