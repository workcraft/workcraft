package org.workcraft.utils;

import org.junit.Assert;
import org.junit.Test;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;

import java.util.Arrays;
import java.util.List;

public class TraceUtilsTest {

    @Test
    public void hasTraceTest() {
        Assert.assertFalse(new Solution().hasTrace());
        Assert.assertTrue(new Solution(null, new Trace()).hasTrace());
        Assert.assertTrue(new Solution(new Trace()).hasTrace());
        Assert.assertTrue(new Solution(new Trace(), new Trace()).hasTrace());
    }

    @Test
    public void serialiseSolutionTest() {
        Assert.assertNull(new Solution().toString());
        Assert.assertEquals("[empty]", new Solution(new Trace()).toString());
        serialiseSolutionCheck(null, -1, null, -1, -1, null);
        serialiseSolutionCheck(Arrays.asList(""), 0, null, -1, -1, "");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 0, null, -1, -1, "a, b, c");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 0, Arrays.asList("d", "e", "f"), 0, -1, "a, b, c\nd, e, f");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 1, Arrays.asList("d", "e", "f"), 2, -1, "1: a, b, c\n2: d, e, f");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 0, null, 2, 1, "a, (b, c)*");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 2, Arrays.asList("d", "e", "f"), 2, 1, "2: a, (b, c)*\n2: d, e, f");
    }

    private void serialiseSolutionCheck(List<String> mainTraceList, int mainPosition,
            List<String> branchTraceList, int branchPosition, int loopPosition, String str) {

        Trace mainTrace = null;
        if (mainTraceList != null) {
            mainTrace = new Trace();
            mainTrace.addAll(mainTraceList);
            mainTrace.setPosition(mainPosition);
        }
        Trace branchTrace = null;
        if (branchTraceList != null) {
            branchTrace = new Trace();
            branchTrace.addAll(branchTraceList);
            branchTrace.setPosition(branchPosition);
        }
        Solution solution = new Solution(mainTrace, branchTrace);
        solution.setLoopPosition(loopPosition);
        Assert.assertEquals(str, TraceUtils.serialiseSolution(solution));
    }

    @Test
    public void deserialiseSolutionTest() {
        deserialiseSolutionCheck("", Arrays.asList(), 0, null, -1, -1);
        deserialiseSolutionCheck("a, b, c", Arrays.asList("a", "b", "c"), 0, null, -1, -1);
        deserialiseSolutionCheck("a, b, c\nd, e, f", Arrays.asList("a", "b", "c"), 0, Arrays.asList("d", "e", "f"), 0, -1);
        deserialiseSolutionCheck("1: a, b, c\n2: d, e, f", Arrays.asList("a", "b", "c"), 1, Arrays.asList("d", "e", "f"), 2, -1);
        deserialiseSolutionCheck("a, (b, c)*", Arrays.asList("a", "b", "c"), 0, null, 2, 1);
        deserialiseSolutionCheck("2: a, (b, c)*\n2: d, e, f", Arrays.asList("a", "b", "c"), 2, Arrays.asList("d", "e", "f"), 2, 1);
    }

    private void deserialiseSolutionCheck(String str, List<String> mainTraceList, int mainPosition,
            List<String> branchTraceList, int branchPosition, int loopPosition) {

        Solution solution = TraceUtils.deserialiseSolution(str);
        Assert.assertEquals(mainTraceList, solution.getMainTrace());
        if (mainTraceList != null) {
            Assert.assertEquals(mainPosition, solution.getMainTrace().getPosition());
        }
        Assert.assertEquals(branchTraceList, solution.getBranchTrace());
        if (branchTraceList != null) {
            Assert.assertEquals(branchPosition, solution.getBranchTrace().getPosition());
        }
        Assert.assertEquals(loopPosition, solution.getLoopPosition());
    }

    @Test
    public void navigateTraceTest() {
        Trace trace = TraceUtils.deserialiseTrace("1: a, b, c");
        Assert.assertTrue(trace.canProgress());
        Assert.assertEquals("b", trace.getCurrent());

        trace.incPosition();
        Assert.assertTrue(trace.canProgress());
        Assert.assertEquals("c", trace.getCurrent());

        trace.incPosition();
        Assert.assertFalse(trace.canProgress());
        Assert.assertNull(trace.getCurrent());

        trace.decPosition();
        Assert.assertEquals(2, trace.getPosition());
        Assert.assertEquals("c", trace.getCurrent());

        trace.remove(1);
        Assert.assertTrue(trace.canProgress());
        Assert.assertEquals(1, trace.getPosition());
        Assert.assertEquals("c", trace.getCurrent());

        trace.removeCurrent();
        Assert.assertFalse(trace.canProgress());
        Assert.assertEquals(1, trace.getPosition());
        Assert.assertNull(trace.getCurrent());

        trace.setPosition(0);
        Assert.assertEquals("a", trace.getCurrent());

        trace.clear();
        Assert.assertEquals(0, trace.getPosition());
        Assert.assertFalse(trace.canProgress());

        Assert.assertEquals("[empty]", trace.toString());
    }

    @Test
    public void processLoopEventTest() {
        processLoopEventCheck(null, false, false);
        processLoopEventCheck("a", false, false);
        processLoopEventCheck("a+", false, true);
        processLoopEventCheck("a-/2", true, false);
        processLoopEventCheck("a~/2", true, true);
    }

    private void processLoopEventCheck(String ref, boolean isFirst, boolean isLast) {
        String str = TraceUtils.addLoopPrefix(ref, isFirst, isLast);
        if (ref == null) {
            Assert.assertNull(str);
        } else {
            Assert.assertNotEquals(ref, str);
        }

        Pair<String, String> pair = TraceUtils.splitLoopPrefix(str);
        Assert.assertEquals(ref, pair.getSecond());
        if (ref == null) {
            Assert.assertEquals("", pair.getFirst());
        } else {
            Assert.assertEquals(str, pair.getFirst() + pair.getSecond());
        }
    }

}
