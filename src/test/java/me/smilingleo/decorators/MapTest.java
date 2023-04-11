package me.smilingleo.decorators;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import me.smilingleo.exceptions.ValidationException;

import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.Tuple;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MapTest {

    @Test(expected = ValidationException.class)
    public void canNotMapToListField() {
        Map map = Map.parse("Map(Items)");
        assertNotNull(map);
        assertEquals(1, map.getArgFieldNames().size());
        assertEquals("Items", map.getArgFieldNames().get(0));

        List<java.util.Map<String, Object>> testData = prepareTestData();
        map.evaluate(testData);
    }

    @Test
    public void testMap() {
        Map map = Map.parse("Map(RatePlanCharge)");
        assertNotNull(map);
        assertEquals(1, map.getArgFieldNames().size());
        assertEquals("RatePlanCharge", map.getArgFieldNames().get(0));

        List<java.util.Map<String, Object>> testData = prepareTestData();
        List<java.util.Map<String, Object>> mapped = map.evaluate(testData);
        assertEquals(5, mapped.size());
    }

    @Test
    public void testMapUniq() {
        String label = "InvoiceItems|Map(RatePlanCharge)|Uniq";
        MergeField field = MergeFieldParser.parse(label);
        assertNotNull(field);
        assertEquals("InvoiceItems", field.getInputFieldName());
        assertEquals(2, field.getDecorators().get().size());

        List<java.util.Map<String, Object>> testData = prepareTestData();
        Optional<Object> result = field.dataBind(testData);
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof List);
        assertEquals(3, ((List)result.get()).size());
    }

    @Test
    public void testMapMultiFields() {
        String label = "Map(ChargeName,'test','world')";
        Map map = Map.parse(label);
        assertNotNull(map);
        assertEquals(1, map.getArgFieldNames().size());
        assertEquals("ChargeName", map.getArgFieldNames().get(0));

        List<java.util.Map<String, Object>> testData = prepareTestData();
        List mapped = map.evaluate(testData);
        assertEquals(5, mapped.size());
        Object head = mapped.get(0);
        assertTrue(head instanceof List);
        assertEquals(3, ((List)head).size());
        assertEquals("charge1", ((List)head).get(0));
        assertEquals("test", ((List)head).get(1));
        assertEquals("world", ((List)head).get(2));
    }

    @Test(expected = ValidationException.class)
    public void canNotDecorateFieldArg() {
        Map.parse("Map(RatePlanCharge|Localise)");
    }

    @Test
    public void parseWithNestedObject() {
        String label = "Map(\"Payment\",EffectiveDate,PaymentNumber,Comment,PaymentMethod.Type,PaymentMethod.AchAccountNumberMask,Amount)";
        Map map = Map.parse(label);
        assertTrue("EffectiveDate should exist.", map.getArgFieldNames().contains("EffectiveDate"));
        assertTrue("PaymentNumber should exist.", map.getArgFieldNames().contains("PaymentNumber"));
        assertTrue("Comment should exist.", map.getArgFieldNames().contains("Comment"));
        assertTrue("PaymentMethod.Type should exist.", map.getArgFieldNames().contains("PaymentMethod.Type"));
        assertTrue("PaymentMethod.AchAccountNumberMask should exist.", map.getArgFieldNames().contains("PaymentMethod.AchAccountNumberMask"));
        assertTrue("Amount should exist.", map.getArgFieldNames().contains("Amount"));

    }

    private List<java.util.Map<String, Object>> prepareTestData() {
        return asList(
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge1"),
                        Tuple.tuple("RatePlanCharge", MapUtils.fromTuples(
                                Tuple.tuple("ChargeNumber", "C-000001"), Tuple.tuple("ChargeModel", "OneTime"))),
                        Tuple.tuple("Items", Arrays.asList(MapUtils.fromTuples(Tuple.tuple("ItemName", "name1"))))),
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
