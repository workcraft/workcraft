package org.workcraft.plugins.cpog.sat;

import org.workcraft.formula.BooleanSolution;
import org.workcraft.plugins.cpog.CpogSettings;

public class ConsoleBooleanSolver {

    public BooleanSolution solve(CnfTask task) {
        String cnf = task.getBody();
        String solution = solve(cnf);
        return SolutionReader.readSolution(task, solution);
    }

    private String solve(String cnf) {
        switch (CpogSettings.getSatSolver()) {
        case CLASP:
            return ProcessIO.runViaStreams(new String[]{CpogSettings.getClaspCommand()}, cnf);
        case MINISAT:
            return ProcessIO.runViaStreams(CpogSettings.getMinisatCommand(), cnf);
        default :
            throw new RuntimeException("Unknown SAT Solver: " + CpogSettings.getSatSolver());
        }
    }
}
