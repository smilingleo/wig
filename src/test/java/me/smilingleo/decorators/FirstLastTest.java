package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.exceptions.ValidationException;

import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.Tuple;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FirstLastTest {

    @Test
    public void testFirst() {
        First first = First.parse("First(2)");
        assertNotNull(first);

        List<java.util.Map<String, Object>> testData = prepareTestData();
        List mapped = first.evaluate(testData);
        assertEquals(2, mapped.size());

        First first2 = First.parse("First(10)");
        assertEquals(5, first2.evaluate(testData).size());
    }

    @Test
    public void testLast() {
        Last last = Last.parse("Last(2)");
        assertNotNull(last);

        List<java.util.Map<String, Object>> testData = prepareTestData();
        List mapped = last.evaluate(testData);
        assertEquals(2, mapped.size());

        Last last2 = Last.parse("Last(10)");
        assertEquals(5, last2.evaluate(testData).size());
    }

    @Test(expected = ValidationException.class)
    public void canOnlyTakePositiveNumber() {
        First.parse("First(-10)");
    }

    @Test(expected = ValidationException.class)
    public void canOnlyTakeNumber() {
        Last.parse("Last(ABC)");
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
                                Tuple.tuple("ChargeNumber", "C-000002"), Tuple.tuple("ChargeModel", "Recurring"))))
        );
    }
}
