package me.smilingleo.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.model.HierarchicalMap;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapUtilsTest {

    @Test
    public void testJsonPath() {
        Map<String, Object> testData = prepareTestData();
        List data = MapUtils.simpleJsonPath(testData, "InvoiceItems.TaxItems.TaxAmount");
        assertNotNull(data);
        assertEquals(6, data.size());

        List data2 = MapUtils.simpleJsonPath(testData, "InvoiceItems.RatePlanCharge");
        assertNotNull(data2);
        assertEquals(2, data2.size());

        // data is by reference
        Map<String, Object> c1 = ((List<Map<String, Object>>) data2).stream()
                .filter(row -> Objects.equals("C-000001", row.get("ChargeNumber")))
                .findFirst()
                .get();
        c1.put("Quantity", 123);
        Map<String, Object> c1InTestData = (Map<String, Object>)((List<Map<String, Object>>) testData.get("InvoiceItems")).get(0).get("RatePlanCharge");
        assertEquals("expect jsonPath returns data reference but not",123, c1InTestData.get("Quantity"));
    }

    @Test
    public void testDottedPath() {
        Map<String, Object> testData = prepareTestData();
        Map<String, Object> first = ((List<Map<String, Object>>) testData.get("InvoiceItems")).get(0);
        Object chargeName = MapUtils.getValueByDottedPath(first, "RatePlanCharge.ChargeNumber");
        assertEquals("C-000001", chargeName);
    }

    @Test(expected = ValidationException.class)
    public void dottedPathNotSupportList() {
        Map<String, Object> testData = prepareTestData();
        Map<String, Object> first = ((List<Map<String, Object>>) testData.get("InvoiceItems")).get(0);
        MapUtils.getValueByDottedPath(first, "TaxItems.TaxName");
    }

    @Test
    public void testGetByDottedPath() {
        Map<String, Object> data = prepareTestData();
        Object value = MapUtils.getByDottedPath(data, "InvoiceItems");
        assertTrue(value instanceof List);

        Map<String, Object> item = (Map<String, Object>) ((List) value).get(0);
        Object chargeNumber = MapUtils.getByDottedPath(item, "RatePlanCharge.ChargeNumber");
        assertEquals("C-000001", chargeNumber);

        Object byWrongKey = MapUtils.getByDottedPath(item, "RatePlanCharge.Unknown");
        assertNull(byWrongKey);
    }

    @Test(expected = ValidationException.class)
    public void testGetByDottedPathWithWrongKey() {
        Map<String, Object> data = prepareTestData();
        MapUtils.getByDottedPath(data, "InvoiceItems.ChargeName");
    }

    @Test
    public void testSetByDottedPath() {
        Map<String, Object> data = new HashMap<>();
        MapUtils.setByDottedPath(data, "a.b.c", "hello");
        assertEquals("hello", MapUtils.getByDottedPath(data, "a.b.c"));

        data = new HierarchicalMap<>();
        MapUtils.setByDottedPath(data, "a.b", "hello");
        assertTrue(data.get("a") instanceof HierarchicalMap);
        assertTrue(((HierarchicalMap)data.get("a")).getParent() == data);
    }

    @Test
    public void testContainsDottedPath() {
        Map<String, Object> testData = new HashMap<String, Object>() {{
            put("Key1", "value1");
            put("Object1", new HashMap<String, Object>(){{
                put("Key1_1", "value1_1");
                put("Object1_1", new HashMap<String, Object>(){{
                    put("Key1_1_1", "value1-1-1");
                }});
            }});
        }};
        assertTrue(MapUtils.containsDottedPath(testData, "."));
        assertTrue(MapUtils.containsDottedPath(testData, "Key1"));
        assertFalse(MapUtils.containsDottedPath(testData, "Key2"));
        assertTrue(MapUtils.containsDottedPath(testData, "Object1.Key1_1"));
        assertFalse(MapUtils.containsDottedPath(testData, "Object1.Key1_2"));
        assertTrue(MapUtils.containsDottedPath(testData, "Object1.Object1_1"));
        assertTrue(MapUtils.containsDottedPath(testData, "Object1.Object1_1.Key1_1_1"));
    }

    private Map<String, Object> prepareTestData() {
        List<Map<String, Object>> rtn = new ArrayList<>();
        rtn.add(MapUtils.fromTuples(
                Tuple.tuple("ChargeName", "charge1"),
                Tuple.tuple("ChargeAmount", 40),
                Tuple.tuple("TaxItems", Arrays.asList(
                        MapUtils.fromTuples(Tuple.tuple("TaxName", "tax1"), Tuple.tuple("TaxAmount", 2)),
                        MapUtils.fromTuples(Tuple.tuple("TaxName", "tax2"), Tuple.tuple("TaxAmount", 1))
                )),
                Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(Tuple.tuple("ChargeNumber", "C-000001"), Tuple.tuple("Quantity", 100)))
        ));
        rtn.add(MapUtils.fromTuples(
                Tuple.tuple("ChargeName", "charge2"),
                Tuple.tuple("ChargeAmount", 30),
                Tuple.tuple("TaxItems", Arrays.asList(
                        MapUtils.fromTuples(Tuple.tuple("TaxName", "tax3"), Tuple.tuple("TaxAmount", 3))
                ))
        ));
        rtn.add(MapUtils.fromTuples(
                Tuple.tuple("ChargeName", "charge2"),
                Tuple.tuple("ChargeAmount", 20),
                Tuple.tuple("TaxItems", Arrays.asList(
                        MapUtils.fromTuples(Tuple.tuple("TaxName", "tax4"), Tuple.tuple("TaxAmount", 4)),
                        MapUtils.fromTuples(Tuple.tuple("TaxName", "tax1"), Tuple.tuple("TaxAmount", 2)),
                        MapUtils.fromTuples(Tuple.tuple("TaxName", "tax2"), Tuple.tuple("TaxAmount", 1))
                )),
                Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(Tuple.tuple("ChargeNumber", "C-000002"), Tuple.tuple("Quantity", 50)))
        ));
        return MapUtils.fromTuples(Tuple.tuple("InvoiceItems", rtn));

    }
}
