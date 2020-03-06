package org.workcraft.traces;

import org.workcraft.utils.TraceUtils;

public class Solution {

    private final Trace mainTrace;
    private final Trace branchTrace;
    private String comment;
    private int loopPosition = -1;

    public Solution(Trace mainTrace) {
        this(mainTrace, null);
    }

    public Solution(Trace mainTrace, Trace branchTrace) {
        this(mainTrace, branchTrace, null);
    }

    public Solution(Trace mainTrace, Trace branchTrace, String comment) {
        this.mainTrace = mainTrace;
        this.branchTrace = branchTrace;
        this.comment = comment;
    }

    public Trace getMainTrace() {
        return mainTrace;
    }

    public Trace getBranchTrace() {
        return branchTrace;
    }

    public boolean hasTrace() {
        return (getMainTrace() != null) || (getBranchTrace() != null);
    }

    public void setComment(String value) {
        comment = value;
    }

    public String getComment() {
        return comment;
    }

    public int getLoopPosition() {
        return adjustLoopPosition(loopPosition);
    }

    public void setLoopPosition(int value) {
        loopPosition = adjustLoopPosition(value);
    }

    private int adjustLoopPosition(int value) {
        if (mainTrace == null) {
            return -1;
        }
        return value < mainTrace.size() ? value : -1;
    }

    public boolean hasLoop() {
        return getLoopPosition() >= 0;
    }

    @Override
    public String toString() {
        String result = TraceUtils.serialiseSolution(this);
        return (result != null) && result.isEmpty() ? TraceUtils.EMPTY_TEXT : result;
    }

}
