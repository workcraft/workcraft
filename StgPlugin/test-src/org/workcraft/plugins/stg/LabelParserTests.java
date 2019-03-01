package org.workcraft.plugins.stg;

import org.junit.Test;
import org.workcraft.types.Triple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LabelParserTests {

    @Test
    public void testNoInstance() {
        Triple<String, SignalTransition.Direction, Integer> result = LabelParser.parseSignalTransition("a+");

        assertEquals("a", result.getFirst());
        assertEquals(SignalTransition.Direction.PLUS, result.getSecond());
        assertEquals(null, result.getThird());
    }

    @Test
    public void testInstance() {
        Triple<String, SignalTransition.Direction, Integer> result = LabelParser.parseSignalTransition("a+/4");

        assertEquals("a", result.getFirst());
        assertEquals(SignalTransition.Direction.PLUS, result.getSecond());
        assertEquals(Integer.valueOf(4), result.getThird());
    }

    @Test
    public void testWrongFormat1() {
        assertNull(LabelParser.parseSignalTransition("x/"));
    }

    @Test
    public void testWrongFormat2() {
        assertNull(LabelParser.parseSignalTransition("x@/3"));
    }

    @Test
    public void testWrongFormat3() {
        assertNull(LabelParser.parseSignalTransition("x-/fifty"));
    }

}