package org.workcraft.plugins.mpsat;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatCombinedChainResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;

public class MpsatUtils {

    public static List<MpsatSolution> getCombinedChainSolutions(Result<? extends MpsatCombinedChainResult> combinedChainResult) {
        if ((combinedChainResult != null) && (combinedChainResult.getOutcome() == Outcome.FINISHED) && (combinedChainResult.getReturnValue() != null)) {
            MpsatCombinedChainResult returnValue = combinedChainResult.getReturnValue();
            List<Result<? extends ExternalProcessResult>> mpsatResultList = returnValue.getMpsatResultList();
            LinkedList<MpsatSolution> solutions = new LinkedList<>();
            for (int index = 0; index < mpsatResultList.size(); ++index) {
                Result<? extends ExternalProcessResult> mpsatResult = mpsatResultList.get(index);
                solutions.addAll(getSolutions(mpsatResult));
            }
            return solutions;
        }
        return null;
    }

    public static List<MpsatSolution> getChainSolutions(Result<? extends MpsatChainResult> chainResult) {
        if ((chainResult != null) && (chainResult.getOutcome() == Outcome.FINISHED) && (chainResult.getReturnValue() != null)) {
            MpsatChainResult returnValue = chainResult.getReturnValue();
            Result<? extends ExternalProcessResult> mpsatResult = returnValue.getMpsatResult();
            return getSolutions(mpsatResult);
        }
        return null;
    }

    public static List<MpsatSolution> getSolutions(Result<? extends ExternalProcessResult> result) {
        if ((result != null) && (result.getOutcome() == Outcome.FINISHED) && (result.getReturnValue() != null)) {
            ExternalProcessResult returnValue = result.getReturnValue();
            return getSolutions(returnValue);
        }
        return null;
    }

    public static List<MpsatSolution> getSolutions(ExternalProcessResult value) {
        MpsatResultParser mdp = new MpsatResultParser(value);
        return mdp.getSolutions();
    }

    public static Boolean getCombinedChainOutcome(Result<? extends MpsatCombinedChainResult> combinedChainResult) {
        List<MpsatSolution> solutions = getCombinedChainSolutions(combinedChainResult);
        if (solutions != null) {
            return hasTraces(solutions);
        }
        return null;
    }

    public static Boolean getChainOutcome(Result<? extends MpsatChainResult> chainResult) {
        List<MpsatSolution> solutions = getChainSolutions(chainResult);
        if (solutions != null) {
            return hasTraces(solutions);
        }
        return null;
    }

    public static boolean hasTraces(List<MpsatSolution> solutions) {
        if (solutions != null) {
            for (MpsatSolution solution : solutions) {
                if ((solution.getMainTrace() != null) || (solution.getBranchTrace() != null)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static  String getToolchainDescription(String title) {
        return "MPSat tool chain" + (title.isEmpty() ? "" : " (" + title + ")");
    }

}
