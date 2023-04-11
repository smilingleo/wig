package me.smilingleo.decorators;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import me.smilingleo.exceptions.ValidationException;

import me.smilingleo.utils.MapUtils;
import me.smilingleo.utils.Tuple;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class FlatMapTest {

    @Test(expected = ValidationException.class)
    public void canNotMapToScalarField() {
        FlatMap flatMap = FlatMap.parse("FlatMap(ChargeName)");
        assertNotNull(flatMap);
        assertEquals(1, flatMap.getArgFieldNames().size());
        assertEquals("ChargeName", flatMap.getArgFieldNames().get(0));

        List<java.util.Map<String, Object>> testData = prepareTestData();
        flatMap.evaluate(testData);
    }

    @Test
    public void testFlatMap() {
        FlatMap flatMap = FlatMap.parse("FlatMap(TaxItems)");
        assertNotNull(flatMap);
        assertEquals(1, flatMap.getArgFieldNames().size());
        assertEquals("TaxItems", flatMap.getArgFieldNames().get(0));

        List<java.util.Map<String, Object>> testData = prepareTestData();
        List<java.util.Map<String, Object>> mapped = flatMap.evaluate(testData);
        assertEquals(8, mapped.size());
    }

    @Test(expected = ValidationException.class)
    public void canNotDecorateFieldArg() {
        Map.parse("FlatMap(TaxItems|First(5))");
    }

    private List<java.util.Map<String, Object>> prepareTestData() {
        return asList(
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge1"),
                        Tuple.tuple("TaxItems", asList(
                                MapUtils.fromTuples(Tuple.tuple("Name", "C-000001"), Tuple.tuple("TaxAmount", 1.0)),
                                MapUtils.fromTuples(Tuple.tuple("Name", "C-000002"), Tuple.tuple("TaxAmount", 1.5)))
                        )
                ),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge2"),
                        Tuple.tuple("TaxItems", asList(
                                MapUtils.fromTuples(Tuple.tuple("Name", "C-000003"), Tuple.tuple("TaxAmount", 2.0)),
                                MapUtils.fromTuples(Tuple.tuple("Name", "C-000002"), Tuple.tuple("TaxAmount", 1.5)))
                        )
                ),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge3")),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge4"),
                        Tuple.tuple("TaxItems", asList(
                                MapUtils.fromTuples(Tuple.tuple("Name", "C-000001"), Tuple.tuple("TaxAmount", 1.0)),
                                MapUtils.fromTuples(Tuple.tuple("Name", "C-000002"), Tuple.tuple("TaxAmount", 1.0)),
                                MapUtils.fromTuples(Tuple.tuple("Name", "C-000003"), Tuple.tuple("TaxAmount", 1.5)))
                        )
                ),
                MapUtils.fromTuples(Tuple.tuple("ChargeName", "charge5"),
                        Tuple.tuple("TaxItems", Arrays.asList(
                                MapUtils.fromTuples(Tuple.tuple("Name", "C-000004"), Tuple.tuple("TaxAmount", 2.5)))
                        )
                )
        );
    }
}
