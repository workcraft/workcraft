package org.workcraft.plugins.stg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Triple;

public class LabelParserTests {

    @Test
    public void testNoInstance() {
        Triple<String, Direction, Integer> result = LabelParser.parseSignalTransition("a+");

        assertEquals("a", result.getFirst());
        assertEquals(Direction.PLUS, result.getSecond());
        assertEquals(null, result.getThird());
    }

    @Test
    public void testInstance() {
        Triple<String, Direction, Integer> result = LabelParser.parseSignalTransition("a+/4");

        assertEquals("a", result.getFirst());
        assertEquals(Direction.PLUS, result.getSecond());
        assertEquals(new Integer(4), result.getThird());
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