package org.geotools.filter.function;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.geotools.filter.FilterFactoryImpl;
import org.junit.Test;
import org.opengis.filter.expression.Function;

import wiremock.com.google.common.collect.Lists;

/**
 * Unit tests for 
 * FilterFunction_lappy
 * FilterFunction_size
 * FilterFunction_literate
 * 
 * @author NielsCharlier
 */
public class ListFunctionsTest {

    FilterFactoryImpl ff = new FilterFactoryImpl();

    @Test
    public void testSize() {
        
        List<Integer> list = Lists.newArrayList(1,2,3,4);
        
        Function exp = ff.function("size", ff.property("."));
        Object value = exp.evaluate(list);
        assertTrue(value instanceof Integer);
        assertEquals(4, ((Integer) value).intValue());
    }
    
    @Test
    public void testLitem() {
        
        List<Integer> list = Lists.newArrayList(1,2,3,4);
        
        Function exp = ff.function("litem", ff.property("."), ff.literal(2));
        Object value = exp.evaluate(list);
        assertTrue(value instanceof Integer);
        assertEquals(3, ((Integer) value).intValue());
    }

    @Test
    public void testLapply() {
        
        List<Integer> list = Lists.newArrayList(1,2,3,4);
        
        Function exp = ff.function("lapply", ff.property("."),
                ff.multiply(ff.property("."), ff.literal(2)));
        Object value = exp.evaluate(list);
        assertTrue(value instanceof List);
        assertEquals(Lists.newArrayList(2.0,4.0,6.0,8.0), ((List<?>) value));
    }

    @Test
    public void testLiterate() {
        List<Integer> list1 = Lists.newArrayList(1,2,3,4);
        List<Integer> list2 = Lists.newArrayList(4,3,2,1);
        
        Function exp = ff.function("literate", ff.property("."),
                ff.multiply(ff.function("litem", ff.literal(list1), ff.property(".")), 
                        ff.function("litem", ff.literal(list2), ff.property("."))));
        Object value = exp.evaluate(4);
        assertTrue(value instanceof List);
        assertEquals(Lists.newArrayList(4.0,6.0,6.0,4.0), ((List<?>) value));
    }
}