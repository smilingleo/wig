package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.utils.MapUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortByTest {

    @Test
    public void testParse() {
        SortBy sortBy = SortBy.parse("SortBy(ChargeAmount,DESC)");
        assertNotNull(sortBy);
        assertEquals(1, sortBy.getArgFieldNames().size());
        assertEquals("ChargeAmount", sortBy.getArgFieldNames().get(0));

        List<Map<String, Object>> testData = prepareTestData();
        assertEquals(40, testData.get(0).get("ChargeAmount"));

        List<Map<String, Object>> sorted = sortBy.evaluate(testData);
        assertEquals("sortBy should not change order of original data", 40, testData.get(0).get("ChargeAmount"));
        assertNotNull(sorted);
        assertEquals(3, sorted.size());
        assertEquals(50, sorted.get(0).get("ChargeAmount"));

        sortBy = SortBy.parse("SortBy(ChargeAmount,ASC)");
        sorted = sortBy.evaluate(testData);
        assertEquals("sortBy should not change order of original data", 40, testData.get(0).get("ChargeAmount"));
        assertEquals(20, sorted.get(0).get("ChargeAmount"));

        sortBy = SortBy.parse("SortBy(UOM,ASC,ChargeAmount,ASC)");
        sorted = sortBy.evaluate(testData);
        assertEquals("sortBy should not change order of original data", 40, testData.get(0).get("ChargeAmount"));
        assertEquals(20, sorted.get(0).get("ChargeAmount"));
        assertEquals(40, sorted.get(1).get("ChargeAmount"));
        assertEquals(50, sorted.get(2).get("ChargeAmount"));

        sortBy = SortBy.parse("SortBy(Child.Amount,ASC)");
        sorted = sortBy.evaluate(testData);
        Assert.assertEquals("sortBy should support dotted path", "Child2",
                MapUtils.getByDottedPath(sorted.get(0), "Child.Name"));

        sortBy = SortBy.parse("SortBy(ChargeName,ASC,Child.Amount,DESC)");
        sorted = sortBy.evaluate(testData);
        assertEquals("sortBy should support multi sort by argument pairs", "Child3",
                MapUtils.getByDottedPath(sorted.get(1), "Child.Name"));

    }

    private List<Map<String, Object>> prepareTestData() {
        List<Map<String, Object>> rtn = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("ChargeName", "charge1");
        record.put("ChargeAmount", 40);
        record.put("UOM", null);
        record.put("Child", new HashMap() {{
            put("Amount", 15);
            put("Name", "Child1");
        }});
        rtn.add(record);

        record = new HashMap<>();
        record.put("ChargeName", "charge2");
        record.put("ChargeAmount", 50);
        record.put("UOM", null);
        record.put("Child", new HashMap() {{
            put("Amount", 10);
            put("Name", "Child2");
        }});
        rtn.add(record);

        record = new HashMap<>();
        record.put("ChargeName", "charge2");
        record.put("ChargeAmount", 20);
        record.put("UOM", null);
        record.put("Child", new HashMap() {{
            put("Amount", 20);
            put("Name", "Child3");
        }});
        rtn.add(record);
        return rtn;
    }
}
