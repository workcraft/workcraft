package org.workcraft.plugins.mpsat.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.SimulationTool;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.PunfOutputParser.Cause;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatUtils {

    public static List<MpsatSolution> getCombinedChainSolutions(Result<? extends MpsatCombinedChainOutput> combinedChainResult) {
        LinkedList<MpsatSolution> solutions = null;
        if (combinedChainResult != null) {
            MpsatCombinedChainOutput combinedChainOutput = combinedChainResult.getPayload();
            if (combinedChainOutput != null) {
                if (combinedChainResult.getOutcome() == Outcome.SUCCESS) {
                    solutions = new LinkedList<>();
                    List<Result<? extends MpsatOutput>> mpsatResultList = combinedChainOutput.getMpsatResultList();
                    for (int index = 0; index < mpsatResultList.size(); ++index) {
                        Result<? extends MpsatOutput> mpsatResult = mpsatResultList.get(index);
                        if (mpsatResult != null) {
                            solutions.addAll(getSolutions(mpsatResult));
                        }
                    }
                } else  if (combinedChainResult.getOutcome() == Outcome.FAILURE) {
                    Result<? extends PunfOutput> punfResult = combinedChainOutput.getPunfResult();
                    if (punfResult != null) {
                        PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
                        Pair<MpsatSolution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
                        if (punfOutcome != null) {
                            Cause cause = punfOutcome.getSecond();
                            boolean isConsistencyCheck = false;
                            if (cause == Cause.INCONSISTENT) {
                                for (MpsatParameters mpsatSettings: combinedChainOutput.getMpsatSettingsList()) {
                                    if (mpsatSettings.getMode() == MpsatMode.STG_REACHABILITY_CONSISTENCY) {
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

    public static List<MpsatSolution> getChainSolutions(Result<? extends MpsatChainOutput> chainResult) {
        LinkedList<MpsatSolution> solutions = null;
        if (chainResult != null) {
            MpsatChainOutput chainOutput = chainResult.getPayload();
            if (chainOutput != null) {
                if (chainResult.getOutcome() == Outcome.SUCCESS) {
                    solutions = new LinkedList<>();
                    Result<? extends MpsatOutput> mpsatResult = chainOutput.getMpsatResult();
                    if (mpsatResult != null) {
                        solutions.addAll(getSolutions(mpsatResult));
                    }
                } else if (chainResult.getOutcome() == Outcome.FAILURE) {
                    Result<? extends PunfOutput> punfResult = chainOutput.getPunfResult();
                    if (punfResult != null) {
                        PunfOutputParser prp = new PunfOutputParser(punfResult.getPayload());
                        Pair<MpsatSolution, PunfOutputParser.Cause> punfOutcome = prp.getOutcome();
                        if (punfOutcome != null) {
                            Cause cause = punfOutcome.getSecond();
                            MpsatParameters mpsatSettings = chainOutput.getMpsatSettings();
                            boolean isConsistencyCheck = (cause == Cause.INCONSISTENT)
                                    && (mpsatSettings.getMode() == MpsatMode.STG_REACHABILITY_CONSISTENCY);
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

    public static List<MpsatSolution> getSolutions(Result<? extends MpsatOutput> result) {
        LinkedList<MpsatSolution> solutions = null;
        if ((result != null) && (result.getOutcome() == Outcome.SUCCESS)) {
            solutions = new LinkedList<>();
            MpsatOutput output = result.getPayload();
            if (output != null) {
                solutions.addAll(getSolutions(output));
            }
        }
        return solutions;
    }

    public static List<MpsatSolution> getSolutions(MpsatOutput output) {
        MpsatOutputParser mdp = new MpsatOutputParser(output);
        return mdp.getSolutions();
    }

    public static Boolean getCombinedChainOutcome(Result<? extends MpsatCombinedChainOutput> combinedChainResult) {
        List<MpsatSolution> solutions = getCombinedChainSolutions(combinedChainResult);
        if (solutions != null) {
            return !hasTraces(solutions);
        }
        return null;
    }

    public static Boolean getChainOutcome(Result<? extends MpsatChainOutput> chainResult) {
        List<MpsatSolution> solutions = getChainSolutions(chainResult);
        if (solutions != null) {
            return !hasTraces(solutions);
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
        String result = "MPSat tool chain";
        if ((title != null) && !title.isEmpty()) {
            result += " (" + title + ")";
        }
        return result;
    }

    public static void playSolution(WorkspaceEntry we, MpsatSolution solution) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        GraphEditorPanel editor = mainWindow.getEditor(we);
        final Toolbox toolbox = editor.getToolBox();
        final SimulationTool tool = toolbox.getToolInstance(SimulationTool.class);
        toolbox.selectTool(tool);
        tool.setTrace(solution.getMainTrace(), solution.getBranchTrace(), editor);
        String comment = solution.getComment();
        if ((comment != null) && !comment.isEmpty()) {
            String traceText = solution.getMainTrace().toText();
            String message = comment.replaceAll("\\<.*?>", "") + " after trace: " + traceText;
            LogUtils.logWarning(message);
        }
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
            DialogUtils.showError("A mutex place must precede a pair of\n" +
                    "non-input transitions, each with a single trigger.\n\n" +
                    "Problematic places are:" +
                    (problematicPlacesString.length() > SizeHelper.getWrapLength() - 20 ? "\n" : " ") +
                    problematicPlacesString);
            return false;
        }
        return true;
    }

    public static ArrayList<MpsatParameters> getMutexImplementabilitySettings(Collection<Mutex> mutexes) {
        final ArrayList<MpsatParameters> settingsList = new ArrayList<>();
        for (Mutex mutex: mutexes) {
            settingsList.add(MpsatParameters.getMutexImplementabilitySettings(mutex));
        }
        return settingsList;
    }

}
