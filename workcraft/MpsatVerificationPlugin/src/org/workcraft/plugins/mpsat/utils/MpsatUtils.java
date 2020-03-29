package org.workcraft.plugins.mpsat.utils;

import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.*;
import org.workcraft.plugins.mpsat.tasks.PunfOutputParser.Cause;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.traces.Solution;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TraceUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MpsatUtils {

    public static List<Solution> getCombinedChainSolutions(Result<? extends CombinedChainOutput> combinedChainResult) {
        LinkedList<Solution> solutions = null;
        if (combinedChainResult != null) {
            CombinedChainOutput combinedChainOutput = combinedChainResult.getPayload();
            if (combinedChainOutput != null) {
                if (combinedChainResult.getOutcome() == Outcome.SUCCESS) {
                    solutions = new LinkedList<>();
                    List<Result<? extends VerificationOutput>> mpsatResultList = combinedChainOutput.getMpsatResultList();
                    for (int index = 0; index < mpsatResultList.size(); ++index) {
                        Result<? extends VerificationOutput> mpsatResult = mpsatResultList.get(index);
                        if (mpsatResult != null) {
                            solutions.addAll(getSolutions(mpsatResult));
                        }
                    }
                } else if (combinedChainResult.getOutcome() == Outcome.FAILURE) {
                    Result<? extends PunfOutput> punfResult = combinedChainOutput.getPunfResult();
                    if (punfResult != null) {
                        PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
                        Pair<Solution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
                        if (punfOutcome != null) {
                            Cause cause = punfOutcome.getSecond();
                            boolean isConsistencyCheck = false;
                            if (cause == Cause.INCONSISTENT) {
                                for (VerificationParameters verificationParameters : combinedChainOutput.getVerificationParametersList()) {
                                    if (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY) {
                                        isConsistencyCheck = true;
                                        break;
                                    }
                                }
                            }
                            if (isConsistencyCheck) {
                                solutions = new LinkedList<>();
                                solutions.add(punfOutcome.getFirst());
                            }
                        }
                    }
                }
            }
        }
        return solutions;
    }

    public static List<Solution> getChainSolutions(Result<? extends VerificationChainOutput> chainResult) {
        LinkedList<Solution> solutions = null;
        if (chainResult != null) {
            VerificationChainOutput chainOutput = chainResult.getPayload();
            if (chainOutput != null) {
                if (chainResult.getOutcome() == Outcome.SUCCESS) {
                    solutions = new LinkedList<>();
                    Result<? extends VerificationOutput> mpsatResult = chainOutput.getMpsatResult();
                    if (mpsatResult != null) {
                        solutions.addAll(getSolutions(mpsatResult));
                    }
                } else if (chainResult.getOutcome() == Outcome.FAILURE) {
                    Result<? extends PunfOutput> punfResult = chainOutput.getPunfResult();
                    if (punfResult != null) {
                        PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
                        Pair<Solution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
                        if (punfOutcome != null) {
                            Cause cause = punfOutcome.getSecond();
                            VerificationParameters verificationParameters = chainOutput.getVerificationParameters();
                            boolean isConsistencyCheck = (cause == Cause.INCONSISTENT)
                                    && (verificationParameters.getMode() == VerificationMode.STG_REACHABILITY_CONSISTENCY);
                            if (isConsistencyCheck) {
                                solutions = new LinkedList<>();
                                solutions.add(punfOutcome.getFirst());
                            }
                        }
                    }
                }
            }
        }
        return solutions;
    }

    public static List<Solution> getSolutions(Result<? extends VerificationOutput> result) {
        LinkedList<Solution> solutions = null;
        if ((result != null) && (result.getOutcome() == Outcome.SUCCESS)) {
            solutions = new LinkedList<>();
            VerificationOutput output = result.getPayload();
            if (output != null) {
                solutions.addAll(getSolutions(output));
            }
        }
        return solutions;
    }

    public static List<Solution> getSolutions(VerificationOutput output) {
        String stdout = output.getStdoutString();
        VerificationOutputParser mdp = new VerificationOutputParser(stdout);
        return mdp.getSolutions();
    }

    public static Boolean getCombinedChainOutcome(Result<? extends CombinedChainOutput> combinedChainResult) {
        List<Solution> solutions = getCombinedChainSolutions(combinedChainResult);
        if (solutions != null) {
            return !TraceUtils.hasTraces(solutions);
        }
        return null;
    }

    public static Boolean getChainOutcome(VerificationChainResultHandlingMonitor monitor) {
        Result<? extends VerificationChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return getChainOutcome(result);
    }

    public static Boolean getChainOutcome(Result<? extends VerificationChainOutput> chainResult) {
        List<Solution> solutions = getChainSolutions(chainResult);
        if (solutions != null) {
            return !TraceUtils.hasTraces(solutions);
        }
        return null;
    }

    public static  String getToolchainDescription(String title) {
        String result = "MPSat tool chain";
        if ((title != null) && !title.isEmpty()) {
            result += " (" + title + ")";
        }
        return result;
    }

    public static boolean mutexStructuralCheck(Stg stg, boolean allowEmptyMutexPlaces) {
        Collection<StgPlace> mutexPlaces = stg.getMutexPlaces();
        if (!allowEmptyMutexPlaces && mutexPlaces.isEmpty()) {
            DialogUtils.showWarning("No mutex places found to check implementability.");
            return false;
        }
        final ArrayList<StgPlace> problematicPlaces = new ArrayList<>();
        for (StgPlace place: mutexPlaces) {
            Mutex mutex = MutexUtils.getMutex(stg, place);
            if (mutex == null) {
                problematicPlaces.add(place);
            }
        }
        if (!problematicPlaces.isEmpty()) {
            String problematicPlacesString = ReferenceHelper.getNodesAsString(stg, problematicPlaces, SizeHelper.getWrapLength());
            DialogUtils.showError("A mutex place must precede two transitions of distinct\n" +
                    "output or internal signals, each with a single trigger.\n\n" +
                    "Problematic places are:" +
                    (problematicPlacesString.length() > SizeHelper.getWrapLength() - 20 ? "\n" : " ") +
                    problematicPlacesString);
            return false;
        }
        return true;
    }

}
