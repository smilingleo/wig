package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.utils.MapUtils;

import me.smilingleo.utils.Tuple;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MaxTest {
    @Test
    public void testDecorator() {
        Max max = Max.parse("Max(ChargeAmount)");
        assertNotNull(max);
        assertEquals(1, max.getArgFieldNames().size());
        assertEquals("ChargeAmount", max.getArgFieldNames().get(0));

        List<Map<String, Object>> testData = prepareTestData();
        Object result = max.evaluate(testData);
        assertNotNull(result);
        assertEquals(50, result);

        max = Max.parse("Max(ChargeName)");
        result = max.evaluate(testData);
        assertNotNull(result);
        assertEquals("charge5", result);
    }

    @Test(expected = ValidationException.class)
    public void maxCanNotTakeDottedPath() {
        Max.parse("Max(RatePlanCharge.ChargeName)");
    }

    private List<Map<String, Object>> prepareTestData() {
        return Arrays.asList(
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge1"), Tuple.tuple("ChargeAmount", 10), Tuple.tuple("Balance", 15.1)),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge2"), Tuple.tuple("ChargeAmount", 20), Tuple.tuple("Balance", 15.1)),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge3"), Tuple.tuple("ChargeAmount", 30), Tuple.tuple("Balance", 15.1)),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge4"), Tuple.tuple("ChargeAmount", 40), Tuple.tuple("Balance", 15.1)),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge5"), Tuple.tuple("ChargeAmount", 50), Tuple.tuple("Balance", 15.1))
        );
    }
}
