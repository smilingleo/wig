package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.exceptions.ValidationException;

import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkipTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testParse() {
        Skip skip = Skip.parse("Skip(3)");
        assertNotNull(skip.toString());
    }

    @Test
    public void testInvalidArgumentsForSkip() {
        expectedEx.expect(ValidationException.class);
        expectedEx.expectMessage("Function Skip requires an integer argument.");
        Skip skip = Skip.parse("Skip(abc)");
    }

    @Test
    public void testNegativeArgumentsForSkip() {
        expectedEx.expect(ValidationException.class);
        expectedEx.expectMessage("Only positive number is allowed as argument of function Skip.");
        Skip skip = Skip.parse("Skip(-1)");
    }

    @Test
    public void testNoArgumentsForSkip() {
        expectedEx.expect(ValidationException.class);
        expectedEx.expectMessage("Function Skip requires 1 argument but it only received 0.");
        Skip skip = Skip.parse("Skip");
    }

    @Test
    public void testInvalidDataFormatForSkip() {
        expectedEx.expect(ValidationException.class);
        expectedEx.expectMessage("Function Skip expects a List type input, but it receives a String.");
        Skip skip = Skip.parse("Skip(3)");
        skip.evaluate("INV0000001");
    }

    @Test
    public void testSkipEvaluate() {
        Skip skip = Skip.parse("Skip(3)");
        List<Map<String, Object>> testData = prepareTestData();
        List mapped = skip.evaluate(testData);
        assertEquals(7, mapped.size());
        assertEquals("charge4", ((HashMap) mapped.get(0)).get("ChargeName"));
        assertEquals("charge5", ((HashMap) mapped.get(1)).get("ChargeName"));
        assertEquals("charge6", ((HashMap) mapped.get(2)).get("ChargeName"));

        skip = Skip.parse("Skip(11)");
        mapped = skip.evaluate(testData);
        assertEquals(0, mapped.size());

        skip = Skip.parse("Skip(10)");
        mapped = skip.evaluate(testData);
        assertEquals(0, mapped.size());
    }

    private List<java.util.Map<String, Object>> prepareTestData() {
        return Arrays.asList(
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge1"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000001"), Tuple.tuple("ChargeModel", "OneTime")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge2"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000002"), Tuple.tuple("ChargeModel", "Recurring")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge3"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000003"), Tuple.tuple("ChargeModel", "Usage")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge4"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000001"), Tuple.tuple("ChargeModel", "OneTime")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge5"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000002"), Tuple.tuple("ChargeModel", "Recurring")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge6"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000001"), Tuple.tuple("ChargeModel", "OneTime")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge7"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000002"), Tuple.tuple("ChargeModel", "Recurring")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge8"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000003"), Tuple.tuple("ChargeModel", "Usage")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge9"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000001"), Tuple.tuple("ChargeModel", "OneTime")))),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge10"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000002"), Tuple.tuple("ChargeModel", "Recurring"))))
        );
    }
}
