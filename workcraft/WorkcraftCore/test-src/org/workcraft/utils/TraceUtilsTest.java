package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;

import java.util.Arrays;
import java.util.List;

class TraceUtilsTest {

    @Test
    void hasTraceTest() {
        Assertions.assertFalse(new Solution(null, null).hasTrace());
        Assertions.assertTrue(new Solution(new Trace()).hasTrace());
        Assertions.assertTrue(new Solution(new Trace(), new Trace()).hasTrace());
        Assertions.assertTrue(new Solution(null, new Trace()).hasTrace());
    }

    @Test
    void serialiseSolutionTest() {
        Assertions.assertEquals("[no trace]", new Solution(null).toString());
        Assertions.assertEquals("[empty trace]", new Solution(new Trace()).toString());
        serialiseSolutionCheck(null, -1, null, -1, -1, null);
        serialiseSolutionCheck(Arrays.asList(""), 0, null, -1, -1, "");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 0, null, -1, -1, "a, b, c");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 0, Arrays.asList("d", "e", "f"), 0, -1, "a, b, c\nd, e, f");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 1, Arrays.asList("d", "e", "f"), 2, -1, "1: a, b, c\n2: d, e, f");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 0, null, 2, 1, "a, {b, c}");
        serialiseSolutionCheck(Arrays.asList("a", "b", "c"), 2, Arrays.asList("d", "e", "f"), 2, 1, "2: a, {b, c}\n2: d, e, f");
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
        Assertions.assertEquals(str, TraceUtils.serialiseSolution(solution));
    }

    @Test
    void deserialiseSolutionTest() {
        deserialiseSolutionCheck("", Arrays.asList(), 0, null, -1, -1);
        deserialiseSolutionCheck("a, b, c", Arrays.asList("a", "b", "c"), 0, null, -1, -1);
        deserialiseSolutionCheck("a, b, c\nd, e, f", Arrays.asList("a", "b", "c"), 0, Arrays.asList("d", "e", "f"), 0, -1);
        deserialiseSolutionCheck("1: a, b, c\n2: d, e, f", Arrays.asList("a", "b", "c"), 1, Arrays.asList("d", "e", "f"), 2, -1);
        deserialiseSolutionCheck("a, {b, c}", Arrays.asList("a", "b", "c"), 0, null, 2, 1);
        deserialiseSolutionCheck("2: a, {b, c}\n2: d, e, f", Arrays.asList("a", "b", "c"), 2, Arrays.asList("d", "e", "f"), 2, 1);
    }

    private void deserialiseSolutionCheck(String str, List<String> mainTraceList, int mainPosition,
            List<String> branchTraceList, int branchPosition, int loopPosition) {

        Solution solution = TraceUtils.deserialiseSolution(str);
        Assertions.assertEquals(mainTraceList, solution.getMainTrace());
        if (mainTraceList != null) {
            Assertions.assertEquals(mainPosition, solution.getMainTrace().getPosition());
        }
        Assertions.assertEquals(branchTraceList, solution.getBranchTrace());
        if (branchTraceList != null) {
            Assertions.assertEquals(branchPosition, solution.getBranchTrace().getPosition());
        }
        Assertions.assertEquals(loopPosition, solution.getLoopPosition());
    }

    @Test
    void navigateTraceTest() {
        Trace trace = TraceUtils.deserialiseTrace("1: a, b, c");
        Assertions.assertTrue(trace.canProgress());
        Assertions.assertEquals("b", trace.getCurrent());

        trace.incPosition();
        Assertions.assertTrue(trace.canProgress());
        Assertions.assertEquals("c", trace.getCurrent());

        trace.incPosition();
        Assertions.assertFalse(trace.canProgress());
        Assertions.assertNull(trace.getCurrent());

        trace.decPosition();
        Assertions.assertEquals(2, trace.getPosition());
        Assertions.assertEquals("c", trace.getCurrent());

        trace.remove(1);
        Assertions.assertTrue(trace.canProgress());
        Assertions.assertEquals(1, trace.getPosition());
        Assertions.assertEquals("c", trace.getCurrent());

        trace.removeCurrent();
        Assertions.assertFalse(trace.canProgress());
        Assertions.assertEquals(1, trace.getPosition());
        Assertions.assertNull(trace.getCurrent());

        trace.setPosition(0);
        Assertions.assertEquals("a", trace.getCurrent());

        trace.clear();
        Assertions.assertEquals(0, trace.getPosition());
        Assertions.assertFalse(trace.canProgress());

        Assertions.assertEquals("[empty trace]", trace.toString());
    }

    @Test
    void processLoopEventTest() {
        processLoopEventCheck(null, false, false);
        processLoopEventCheck("a", false, false);
        processLoopEventCheck("a+", false, true);
        processLoopEventCheck("a-/2", true, false);
        processLoopEventCheck("a~/2", true, true);
    }

    private void processLoopEventCheck(String ref, boolean isFirst, boolean isLast) {
        String str = TraceUtils.addLoopDecoration(ref, isFirst, isLast);
        if (ref == null) {
            Assertions.assertNull(str);
        } else {
            Assertions.assertNotEquals(ref, str);
        }

        Pair<String, String> pair = TraceUtils.splitLoopDecoration(str);
        Assertions.assertEquals(ref, pair.getSecond());
        if (ref == null) {
            Assertions.assertEquals("", pair.getFirst());
        } else {
            Assertions.assertEquals(str, pair.getFirst() + pair.getSecond());
        }
    }

}
