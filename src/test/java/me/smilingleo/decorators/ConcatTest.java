package me.smilingleo.decorators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import me.smilingleo.exceptions.ValidationException;
import me.smilingleo.model.HierarchicalMap;

import org.junit.Test;

import java.util.stream.Collectors;

public class ConcatTest {

    @Test
    public void testParse() {
        String label = ".|Concat(Field1,Field2,'_')";
        MergeField mergeField = MergeFieldParser.parse(label);
        assertNotNull(mergeField);
        assertEquals(".", mergeField.getInputFieldName());
        assertTrue(mergeField.getDecorators().isPresent());
        assertEquals(1, mergeField.getDecorators().get().size());
        Decorator decorator = mergeField.getDecorators().get().get(0);
        assertTrue(decorator instanceof Concat);
        Concat concat = (Concat) decorator;
        assertEquals("Field1,Field2", concat.getArgFieldNames().stream().collect(Collectors.joining(",")));
        assertEquals("'_'", concat.delimiter);
        assertEquals(".", concat.getInputFieldName());
        assertEquals(label.substring(2), concat.toString());
    }

    @Test(expected = ValidationException.class)
    public void atLeastOneInputField() {
        Concat.parse("Concat('_')");
    }

    @Test(expected = ValidationException.class)
    public void mustHaveArgument() {
        Concat.parse("Concat");
    }

    @Test
    public void nullInputReturnNull() {
        assertNull(Concat.parse("Concat(field1,'_')").evaluate(null));
    }

    @Test(expected = ValidationException.class)
    public void onlyAcceptMapInput() {
        Concat.parse("Concat(field1,field2,'_')").evaluate("invalid input");
    }

    @Test
    public void complyWithMustacheContextObjectFinding() {
        HierarchicalMap parent = new HierarchicalMap();
        parent.put("field1", "value1");
        HierarchicalMap object = new HierarchicalMap() {{
            put("field2_2", "value2_2");
        }};
        parent.put("field2", object);
        // field1 is not available in `object`, but it's available in its parent object.
        // we should be able to find it.
        Concat concat = Concat.parse("Concat(field1,field2_2,'_')");
        Object result = concat.evaluate(object);
        assertEquals("value1_value2_2", result);
    }
}
