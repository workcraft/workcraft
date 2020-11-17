package org.workcraft.traces;

import org.workcraft.utils.TraceUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Solution {

    private static final String EMPTY_TEXT = "[no trace]";

    private final Trace mainTrace;
    private final Trace branchTrace;
    private String comment;
    private int loopPosition = -1;
    private final Set<Trace> continuations = new HashSet<>();

    public Solution(Trace mainTrace) {
        this(mainTrace, null);
    }

    public Solution(Trace mainTrace, Trace branchTrace) {
        this(mainTrace, branchTrace, null);
    }

    public Solution(Trace mainTrace, Trace branchTrace, String comment) {
        this(mainTrace, branchTrace, comment, null);
    }

    public Solution(Trace mainTrace, Trace branchTrace, String comment, Collection<Trace> continuations) {
        this.mainTrace = mainTrace;
        this.branchTrace = branchTrace;
        this.comment = comment;
        if (continuations != null) {
            this.continuations.addAll(continuations);
        }
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

    public void addContinuation(Trace continuation) {
        continuations.add(continuation);
    }

    public void removeContinuation(Trace continuation) {
        continuations.remove(continuation);
    }

    public void clearContinuations() {
        continuations.clear();
    }

    public Set<Trace> getContinuations() {
        return Collections.unmodifiableSet(continuations);
    }

    @Override
    public String toString() {
        String result = TraceUtils.serialiseSolution(this);
        return result == null ? EMPTY_TEXT : result.isEmpty() ? new Trace().toString() : result;
    }

}
