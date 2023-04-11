package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.TestDataUtils;
import me.smilingleo.utils.Tuple;

import me.smilingleo.utils.Constants;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GroupByTest {
    @Test
    public void testParse() {
        GroupBy groupBy = GroupBy.parse("GroupBy(ChargeName)");
        assertNotNull(groupBy);
        assertEquals(1, groupBy.getArgFieldNames().size());
        assertEquals("ChargeName", groupBy.getArgFieldNames().get(0));

        List<Map<String, Object>> testData = prepareTestData();
        List<Map<String, Object>> grouped = groupBy.evaluate(testData);
        assertNotNull(grouped);
        assertEquals(2, grouped.size());
    }

    @Test
    public void testWithNestedDecorator() {
        String label = "GroupBy(ChargeName|Substr(0,4))";
        GroupBy groupBy = GroupBy.parse(label);
        assertNotNull(groupBy);
        assertEquals(1, groupBy.getArgFieldNames().size());
        assertEquals("ChargeName", groupBy.getArgFieldNames().get(0));
        // test toString()
        assertEquals(label, groupBy.toString());

        List<Map<String, Object>> testData = prepareTestData();
        List<Map<String, Object>> grouped = groupBy.evaluate(testData);
        assertNotNull(grouped);
        assertEquals(1, grouped.size());
    }

    @Test
    public void testMultiGroupBy() {
        Map<String, Object> data = TestDataUtils.loadJsonAsMap("sample-input.json");
        MergeField mergeField = MergeFieldParser.parse("InvoiceItems|GroupBy(ChargeName,ServiceStartDate|Substr(0,6),Id)");

        Optional<Object> opt = mergeField.dataBind(
                MapUtils.getByDottedPath(data, "Invoice.InvoiceItems"));
        assertTrue(opt.isPresent());
        assertTrue(opt.get() instanceof List);
        Map<String, Object> row1 = (Map<String, Object>) ((List<?>) opt.get()).get(0);
        List<Map<String, Object>> items = MapUtils.simpleJsonPath(row1,
                String.format("%s.%s.%s", Constants.DERIVED_LIST_KEY, Constants.DERIVED_LIST_KEY, Constants.DERIVED_LIST_KEY));
        assertEquals(7, items.size());
        assertTrue(items.stream().allMatch(item -> item.get("ChargeName").equals("Discount Charge")));

        Map<String, Object> row2 = (Map<String, Object>) ((List<?>) opt.get()).get(1);
        List<Map<String, Object>> items2 = MapUtils.simpleJsonPath(row2,
                String.format("%s.%s.%s", Constants.DERIVED_LIST_KEY, Constants.DERIVED_LIST_KEY, Constants.DERIVED_LIST_KEY));
        assertEquals(7, items2.size());
        assertTrue(items2.stream().allMatch(item -> item.get("ChargeName").equals("Loyalty Discount")));

        Map<String, Object> row3 = (Map<String, Object>) ((List<?>) opt.get()).get(2);
        List<Map<String, Object>> items3 = MapUtils.simpleJsonPath(row3,
                String.format("%s.%s.%s", Constants.DERIVED_LIST_KEY, Constants.DERIVED_LIST_KEY, Constants.DERIVED_LIST_KEY));
        assertEquals(7, items3.size());
        assertTrue(items3.stream().allMatch(item -> item.get("ChargeName").equals("Service Charge")));
    }

    @Test
    public void testGroupByDottedPath() {
        Map<String, Object> data = TestDataUtils.loadJsonAsMap("sample-input.json");

        MergeField mergeField = MergeFieldParser.parse("InvoiceItems|GroupBy(RatePlanCharge.ChargeNumber)");
        Optional<Object> opt = mergeField.dataBind(
                MapUtils.getByDottedPath(data, "Invoice.InvoiceItems"));
        assertTrue(opt.isPresent());
        assertTrue(opt.get() instanceof List);
    }

    private List<Map<String, Object>> prepareTestData() {
        List<Map<String, Object>> rtn = new ArrayList<>();
        rtn.add(MapUtils.fromTuples(
                Tuple.tuple("ChargeName", "charge1"),
                Tuple.tuple("ChargeAmount", 40)
        ));
        rtn.add(MapUtils.fromTuples(
                Tuple.tuple("ChargeName", "charge2"),
                Tuple.tuple("ChargeAmount", 30)
        ));
        rtn.add(MapUtils.fromTuples(
                Tuple.tuple("ChargeName", "charge2"),
                Tuple.tuple("ChargeAmount", 20)
        ));
        return rtn;
    }
}
