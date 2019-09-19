package org.workcraft.plugins.cpog.sat;

import org.workcraft.formula.BooleanEvaluator;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanSolution;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.plugins.cpog.encoding.Encoding;
import org.workcraft.formula.utils.SolutionSubstitutor;

public class SolutionPrettifier {

    public static <T> Encoding prettifySolution(OptimisationTask<T> task, BooleanSolution solution) {
        if (solution == null) {
            return null;
        }

        BooleanFormula[] functionVars = task.getFunctionVars();
        if (functionVars == null) {
            throw new RuntimeException("functionVars is null");
        }

        BooleanFormula[][] encodingVars = task.getEncodingVars();
        if (encodingVars == null) {
            throw new RuntimeException("encodingVars is null");
        }

        BooleanVisitor<BooleanFormula> substitutor = new SolutionSubstitutor(solution);
        BooleanVisitor<Boolean> evaluator = new BooleanEvaluator();

        BooleanFormula[] functions = new BooleanFormula[functionVars.length];

        for (int i = 0; i < functions.length; i++) {
            functions[i] = functionVars[i].accept(substitutor);
        }

        boolean[][] encoding = new boolean[encodingVars.length][];
        for (int i = 0; i < encodingVars.length; i++) {
            encoding[i] = new boolean[encodingVars[i].length];
            for (int j = 0; j < encodingVars[i].length; j++) {
                encoding[i][j] = encodingVars[i][j].accept(substitutor).accept(evaluator);
            }
        }

        return new Encoding(functions, encoding);
    }

}
