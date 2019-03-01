package org.workcraft.formula.sat;

import org.workcraft.formula.BooleanSolution;
import org.workcraft.formula.cnf.CnfTask;
import org.workcraft.plugins.builtin.settings.CommonSatSettings;

public class ConsoleBooleanSolver {

    public BooleanSolution solve(CnfTask task) {
        String cnf = task.getBody();
        String solution = solve(cnf);
        return SolutionReader.readSolution(task, solution);
    }

    private String solve(String cnf) {
        switch (CommonSatSettings.getSatSolver()) {
        case CLASP:
            return ProcessIO.runViaStreams(new String[]{CommonSatSettings.getClaspCommand()}, cnf);
        case MINISAT:
            return ProcessIO.runViaStreams(CommonSatSettings.getMinisatCommand(), cnf);
        default :
            throw new RuntimeException("Unknown SAT Solver: " + CommonSatSettings.getSatSolver());
        }
    }
}
