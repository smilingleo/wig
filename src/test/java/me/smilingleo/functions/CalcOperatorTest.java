package me.smilingleo.functions;

import static org.junit.Assert.assertEquals;

import me.smilingleo.exceptions.ValidationException;

import org.junit.Test;

public class CalcOperatorTest {

    @Test
    public void testAdd() {
        assertEquals("", CalcOperator.Add.calulate(null, null));
        assertEquals("2", CalcOperator.Add.calulate(null, 2));
        assertEquals("2", CalcOperator.Add.calulate(2, null));
        assertEquals("5", CalcOperator.Add.calulate(2, 3));
        assertEquals("5.5555", CalcOperator.Add.calulate(2.2222, 3.3333));
    }

    @Test(expected = ValidationException.class)
    public void testAddWithTextValue() {
        CalcOperator.Add.calulate("hello", 3);
    }

    @Test
    public void testSubtract() {
        assertEquals("", CalcOperator.Subtract.calulate(null, null));
        assertEquals("-2", CalcOperator.Subtract.calulate(null, 2));
        assertEquals("2", CalcOperator.Subtract.calulate(2, null));
        assertEquals("-1", CalcOperator.Subtract.calulate(2, 3));
        assertEquals("-1.1111", CalcOperator.Subtract.calulate(2.2222, 3.3333));
    }

    @Test(expected = ValidationException.class)
    public void testSubtractWithTextValue() {
        CalcOperator.Subtract.calulate("hello", 3);
    }

    @Test
    public void testMultiply() {
        assertEquals("", CalcOperator.Multiply.calulate(null, null));
        assertEquals("", CalcOperator.Multiply.calulate(null, 2));
        assertEquals("", CalcOperator.Multiply.calulate(2, null));
        assertEquals("6", CalcOperator.Multiply.calulate(2, 3));
        assertEquals("7.40725926", CalcOperator.Multiply.calulate(2.2222, 3.3333));
    }

    @Test(expected = ValidationException.class)
    public void testMultiplyWithTextValue() {
        CalcOperator.Multiply.calulate("hello", 3);
    }

    @Test
    public void testDivide() {
        assertEquals("", CalcOperator.Divide.calulate(null, null));
        assertEquals("", CalcOperator.Divide.calulate(null, 2));
        assertEquals("", CalcOperator.Divide.calulate(2, null));
        assertEquals("0.5", CalcOperator.Divide.calulate(2, 4));
        assertEquals("1.1111", CalcOperator.Divide.calulate(2.2222, 2));
    }

    @Test(expected = ValidationException.class)
    public void testDivideWithTextValue() {
        CalcOperator.Divide.calulate("hello", 3);
    }

    @Test
    public void testAnd() {
        assertEquals(false, CalcOperator.And.calulate(null, null));
        assertEquals(false, CalcOperator.And.calulate(null, true));
        assertEquals(false, CalcOperator.And.calulate(true, null));
        assertEquals(true, CalcOperator.And.calulate(true, true));
        assertEquals(false, CalcOperator.And.calulate(true, false));
        assertEquals(true, CalcOperator.And.calulate(true, "true"));
    }

    @Test(expected = ValidationException.class)
    public void testAndWithTextValue() {
        CalcOperator.And.calulate("true", 3);
    }

    @Test
    public void testOr() {
        assertEquals(false, CalcOperator.Or.calulate(null, null));
        assertEquals(true, CalcOperator.Or.calulate(null, true));
        assertEquals(true, CalcOperator.Or.calulate(true, null));
        assertEquals(false, CalcOperator.Or.calulate(false, null));
        assertEquals(true, CalcOperator.Or.calulate(true, true));
        assertEquals(true, CalcOperator.Or.calulate(false, true));
        assertEquals(true, CalcOperator.Or.calulate(false, "true"));
    }

    @Test(expected = ValidationException.class)
    public void testOrWithTextValue() {
        CalcOperator.Or.calulate("true", 3);
    }

    @Test
    public void testXor() {
        assertEquals(false, CalcOperator.Xor.calulate(null, null));
        assertEquals(true, CalcOperator.Xor.calulate(null, true));
        assertEquals(true, CalcOperator.Xor.calulate(true, null));
        assertEquals(true, CalcOperator.Xor.calulate(false, null));
        assertEquals(false, CalcOperator.Xor.calulate(true, true));
        assertEquals(true, CalcOperator.Xor.calulate(false, true));
        assertEquals(true, CalcOperator.Xor.calulate(false, "true"));
    }

    @Test(expected = ValidationException.class)
    public void testXorWithTextValue() {
        CalcOperator.Xor.calulate("true", 3);
    }


}
