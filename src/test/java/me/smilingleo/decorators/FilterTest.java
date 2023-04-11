package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.RenderContext;
import me.smilingleo.utils.MapUtils;

import me.smilingleo.utils.Tuple;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FilterTest {
    @Test
    public void testDecorator() {
        FilterByValue filter = FilterByValue.parse("FilterByValue(ChargeName,GT,charge2)");
        assertNotNull(filter);
        assertEquals(1, filter.getArgFieldNames().size());
        assertEquals("ChargeName", filter.getArgFieldNames().get(0));

        List<Map<String, Object>> testData = prepareTestData();
        List<Map<String, Object>> filtered = filter.evaluate(testData);
        assertNotNull(filtered);
        assertEquals(3, filtered.size());

        FilterByValue filter2 = FilterByValue.parse("FilterByValue(ChargeName,NE,charge2)");
        List<Map<String, Object>> filtered2 = filter2.evaluate(testData);
        assertNotNull(filtered2);
        assertEquals(4, filtered2.size());

        FilterByRef filter3 = FilterByRef.parse("FilterByRef(ChargeAmount,LT,Balance)");
        List<Map<String, Object>> filtered3 = filter3.evaluate(testData);
        assertNotNull(filtered3);
        assertEquals(1, filtered3.size());
    }

    @Test
    public void testFilterByRefByGettingValueFromHierarchicalMap(){
        FilterByRef filter = FilterByRef.parse("FilterByRef(ChargeAmount,GT,Balance)");
        List<Map<String, Object>> filtered = filter.evaluate(prepareTestData());
        assertNotNull(filtered);
        assertEquals(4, filtered.size());
    }

    @Test
    public void testFilterByRefByGettingValueFromRenderContext(){
        RenderContext.getContext().assignVariable("Amount", 30);
        FilterByRef filter = FilterByRef.parse("FilterByRef(ChargeAmount,GE,Amount)");
        List<Map<String, Object>> filtered = filter.evaluate(prepareTestData());
        assertNotNull(filtered);
        assertEquals(3, filtered.size());
    }

    @Test
    public void testFilterByRefInvalidReferenceName(){
        FilterByRef filter = FilterByRef.parse("FilterByRef(ChargeAmount,GE,Bal)");
        List<Map<String, Object>> filtered = filter.evaluate(prepareTestData());
        assertNotNull(filtered);
        assertEquals(0, filtered.size());
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
