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

public class SumTest {
    @Test
    public void testDecorator() {
        Sum sum = Sum.parse("Sum(Balance)");
        assertNotNull(sum);
        assertEquals(1, sum.getArgFieldNames().size());
        assertEquals("Balance", sum.getArgFieldNames().get(0));

        List<Map<String, Object>> testData = prepareTestData();
        Double result = sum.evaluate(testData);
        assertEquals(Double.valueOf(15.1*5), result);

        Sum sum2 = Sum.parse("Sum(ChargeAmount)");
        Double result2 = sum2.evaluate(testData);
        assertEquals(Double.valueOf(100), result2);

        Sum sum3 = Sum.parse("Sum(field__c)");
        Double result3 = sum3.evaluate(testData);
        assertEquals(Double.valueOf(54.6), result3);
    }

    @Test(expected = ValidationException.class)
    public void onlyTakeNumericField() {
        List<Map<String, Object>> testData = prepareTestData();
        Sum sum = Sum.parse("Sum(ChargeName)");
        sum.evaluate(testData);
    }

    private List<Map<String, Object>> prepareTestData() {
        return Arrays.asList(
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge1"), Tuple.tuple("ChargeAmount", 10), Tuple.tuple("Balance", 15.1), Tuple.tuple("field__c", "15.13")),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge2"), Tuple.tuple("ChargeAmount", 20), Tuple.tuple("Balance", 15.1), Tuple.tuple("field__c", "10.12")),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge3"), Tuple.tuple("ChargeAmount", 30), Tuple.tuple("Balance", 15.1), Tuple.tuple("field__c", "14.18")),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge4"), Tuple.tuple("ChargeAmount", 40), Tuple.tuple("Balance", 15.1), Tuple.tuple("field__c", "15.17")),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge5"), Tuple.tuple("Balance", 15.1))
        );
    }
}
