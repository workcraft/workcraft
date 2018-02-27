package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Trace;

public class MpsatSolution {
    private final Trace mainTrace;
    private final Trace branchTrace;
    private String comment;

    public MpsatSolution(MpsatSolution solution) {
        this(solution.getMainTrace(), solution.getBranchTrace(), solution.getComment());
    }

    public MpsatSolution(Trace mainTrace, Trace branchTrace) {
        this(mainTrace, branchTrace, null);
    }

    public MpsatSolution(Trace mainTrace, Trace branchTrace, String comment) {
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

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public String toString() {
        String result = "";
        if (mainTrace != null) {
            result += mainTrace.toString();
        }
        if (branchTrace != null) {
            result += "\n";
            result += branchTrace.toString();
        }
        return result;
    }

}
