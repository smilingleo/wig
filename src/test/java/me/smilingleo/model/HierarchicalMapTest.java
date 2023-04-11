package me.smilingleo.model;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import me.smilingleo.utils.MapUtils;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class HierarchicalMapTest {
    @Test
    public void testHierarchicalMap() {
        HierarchicalMap<String, Object> account = new HierarchicalMap<String, Object>() {{
            put("Name", "Account Name");
            put("Balance", 21);
        }};
        HierarchicalMap<String, Object> item1 = new HierarchicalMap<String, Object>() {{
            put("ChargeName", "charge1");
            put("ChargeAmount", 10);
            put("Balance", 10);
        }};
        HierarchicalMap<String, Object> item2 = new HierarchicalMap<String, Object>() {{
            put("ChargeName", "charge2");
            put("ChargeAmount", 11);
            put("Balance", 0);
        }};

        HierarchicalMap<String, Object> invoice = new HierarchicalMap<String, Object>() {{
            put("InvoiceNumber", "INV-00001");
            put("Account", account);
            put("InvoiceItems", asList( item1, item2));
        }};
        HierarchicalMap root = new HierarchicalMap() {{
            put("Invoice", invoice);
        }};

        assertTrue(invoice.getParent() == root);
        assertTrue(account.getParent() == invoice);
        assertTrue(item1.getParent() == invoice);
        assertTrue(item2.getParent() == invoice);
    }
    @Test
    public void testFromMap() {
        HashMap<String, Object> account = new HashMap<String, Object>() {{
            put("Name", "Account Name");
            put("Balance", 21);
        }};
        HashMap<String, Object> item1 = new HashMap<String, Object>() {{
            put("ChargeName", "charge1");
            put("ChargeAmount", 10);
            put("Balance", 10);
        }};
        HashMap<String, Object> item2 = new HashMap<String, Object>() {{
            put("ChargeName", "charge2");
            put("ChargeAmount", 11);
            put("Balance", 0);
        }};

        HashMap<String, Object> invoice = new HashMap<String, Object>() {{
            put("InvoiceNumber", "INV-00001");
            put("Account", account);
            put("InvoiceItems", asList( item1, item2));
        }};
        HashMap root = new HashMap() {{
            put("Invoice", invoice);
        }};

        HierarchicalMap hmap = HierarchicalMap.fromMap(root);

        assertNotNull(hmap);
        assertNotNull(hmap.get("Invoice") instanceof HierarchicalMap);

        List items = MapUtils.simpleJsonPath(hmap, "Invoice.InvoiceItems");
        assertEquals(2, items.size());
        assertTrue(items.get(0) instanceof HierarchicalMap);
        assertTrue(((HierarchicalMap) items.get(0)).getParent() == hmap.get("Invoice"));
    }
}
