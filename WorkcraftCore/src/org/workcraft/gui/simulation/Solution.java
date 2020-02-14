package org.workcraft.gui.simulation;

public class Solution {

    private final Trace mainTrace;
    private final Trace branchTrace;
    private String comment;
    private int loopPosition = -1;

    public Solution() {
        this(null, null);
    }

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

    private boolean hasLoop() {
        return getLoopPosition() >= 0;
    }

    @Override
    public String toString() {
        String result = "";
        if (!hasLoop()) {
            result += mainTrace.toString();
        } else {
            // Prefix
            Trace prefixTrace = new Trace();
            prefixTrace.addAll(mainTrace.subList(0, loopPosition));
            result += prefixTrace.toString();
            // Loop
            Trace loopTrace = new Trace();
            loopTrace.addAll(mainTrace.subList(loopPosition, mainTrace.size()));
            if (!result.isEmpty()) result += ", ";
            result += "(" + loopTrace.toString() + ")*";
        }
        if (branchTrace != null) {
            result += "\n";
            result += branchTrace.toString();
        }
        return result;
    }

}
