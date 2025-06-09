package org.workcraft.plugins.cpog.sat;

import org.workcraft.plugins.cpog.CpogSettings;

public class ConsoleBooleanSolver {

    public BooleanSolution solve(CnfTask task) {
        String cnf = task.getBody();
        String solution = solve(cnf);
        return SolutionReader.readSolution(task, solution);
    }

    private String solve(String cnf) {
        return switch (CpogSettings.getSatSolver()) {
            case CLASP -> ProcessIO.runViaStreams(new String[]{CpogSettings.getClaspCommand()}, cnf);
            case MINISAT -> ProcessIO.runViaStreams(CpogSettings.getMinisatCommand(), cnf);
        };
    }
}
