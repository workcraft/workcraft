package org.workcraft.formula.sat;

import org.workcraft.formula.BooleanSolution;
import org.workcraft.plugins.shared.CommonSatSettings;

public class ConsoleBooleanSolver {

    public BooleanSolution solve(CnfTask task) {
        String cnf = task.getBody();
        String solution = solve(cnf);
        return SolutionReader.readSolution(task, solution);
    }

    private String solve(String cnf) {
        switch (CommonSatSettings.getSatSolver()) {
        case CLASP:
            return ProcessIO.runViaStreams(cnf, new String[]{CommonSatSettings.getClaspCommand()});
        case MINISAT:
            return ProcessIO.minisat(CommonSatSettings.getMinisatCommand(), cnf);
        default :
            throw new RuntimeException("Unknown SAT Solver: " + CommonSatSettings.getSatSolver());
        }
    }
}
