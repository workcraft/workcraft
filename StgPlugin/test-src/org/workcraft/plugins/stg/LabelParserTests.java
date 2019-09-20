package org.workcraft.plugins.stg;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.types.Triple;

public class LabelParserTests {

    @Test
    public void testNoInstance() {
        Triple<String, SignalTransition.Direction, Integer> result = LabelParser.parseSignalTransition("a+");

        Assert.assertEquals("a", result.getFirst());
        Assert.assertEquals(SignalTransition.Direction.PLUS, result.getSecond());
        Assert.assertEquals(null, result.getThird());
    }

    @Test
    public void testInstance() {
        Triple<String, SignalTransition.Direction, Integer> result = LabelParser.parseSignalTransition("a+/4");

        Assert.assertEquals("a", result.getFirst());
        Assert.assertEquals(SignalTransition.Direction.PLUS, result.getSecond());
        Assert.assertEquals(Integer.valueOf(4), result.getThird());
    }

    @Test
    public void testWrongFormat1() {
        Assert.assertNull(LabelParser.parseSignalTransition("x/"));
    }

    @Test
    public void testWrongFormat2() {
        Assert.assertNull(LabelParser.parseSignalTransition("x@/3"));
    }

    @Test
    public void testWrongFormat3() {
        Assert.assertNull(LabelParser.parseSignalTransition("x-/fifty"));
    }

}