package org.workcraft.plugins.stg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.types.Triple;

class LabelParserTests {

    @Test
    void testNoInstance() {
        Triple<String, SignalTransition.Direction, Integer> result = LabelParser.parseSignalTransition("a+");

        Assertions.assertEquals("a", result.getFirst());
        Assertions.assertEquals(SignalTransition.Direction.PLUS, result.getSecond());
        Assertions.assertEquals(null, result.getThird());
    }

    @Test
    void testInstance() {
        Triple<String, SignalTransition.Direction, Integer> result = LabelParser.parseSignalTransition("a+/4");

        Assertions.assertEquals("a", result.getFirst());
        Assertions.assertEquals(SignalTransition.Direction.PLUS, result.getSecond());
        Assertions.assertEquals(Integer.valueOf(4), result.getThird());
    }

    @Test
    void testWrongFormat1() {
        Assertions.assertNull(LabelParser.parseSignalTransition("x/"));
    }

    @Test
    void testWrongFormat2() {
        Assertions.assertNull(LabelParser.parseSignalTransition("x@/3"));
    }

    @Test
    void testWrongFormat3() {
        Assertions.assertNull(LabelParser.parseSignalTransition("x-/fifty"));
    }

}