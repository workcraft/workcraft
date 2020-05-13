package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.types.Triple;

public class LabelParserTests {

    @Test
    public void testNoInstance() {
        Triple<String, SignalTransition.Direction, Integer> result = LabelParser.parseSignalTransition("a+");

        Assertions.assertEquals("a", result.getFirst());
        Assertions.assertEquals(SignalTransition.Direction.PLUS, result.getSecond());
        Assertions.assertEquals(null, result.getThird());
    }

    @Test
    public void testInstance() {
        Triple<String, SignalTransition.Direction, Integer> result = LabelParser.parseSignalTransition("a+/4");

        Assertions.assertEquals("a", result.getFirst());
        Assertions.assertEquals(SignalTransition.Direction.PLUS, result.getSecond());
        Assertions.assertEquals(Integer.valueOf(4), result.getThird());
    }

    @Test
    public void testWrongFormat1() {
        Assertions.assertNull(LabelParser.parseSignalTransition("x/"));
    }

    @Test
    public void testWrongFormat2() {
        Assertions.assertNull(LabelParser.parseSignalTransition("x@/3"));
    }

    @Test
    public void testWrongFormat3() {
        Assertions.assertNull(LabelParser.parseSignalTransition("x-/fifty"));
    }

}