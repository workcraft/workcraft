package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.dialogs.ReachabilityDialog;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.projection.Enabledness;
import org.workcraft.plugins.mpsat_verification.projection.ProjectionBuilder;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.OutcomeUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CompositionOutputInterpreter extends AbstractCompositionOutputInterpreter {

    private final List<WorkspaceEntry> wes;

    public CompositionOutputInterpreter(List<WorkspaceEntry> wes, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, boolean interactive) {

        super(wes.get(0), exportOutput, pcompOutput, mpsatOutput, interactive);
        this.wes = wes;
    }

    private ComponentData getComponentData(WorkspaceEntry we) {
        CompositionData compositionData = getCompositionData();
        return compositionData == null ? null : compositionData.getComponentData(wes.indexOf(we));
    }

    @Override
    public void reportSolutions(String message, List<Solution> solutions) {
        Framework framework = Framework.getInstance();
        if (isInteractive() && framework.isInGuiMode()) {
            MainWindow mainWindow = framework.getMainWindow();
            for (WorkspaceEntry we : wes) {
                List<Solution> processedSolutions = processSolutions(solutions, we);
                if (!processedSolutions.isEmpty()) {
                    mainWindow.requestFocus(we);
                    String title = OutcomeUtils.TITLE + " for model '" + we.getTitle() + "'";
                    ReachabilityDialog solutionsDialog = new ReachabilityDialog(
                            mainWindow, we, title, message, processedSolutions);

                    solutionsDialog.reveal();
                }
            }
        }
    }

    public List<Solution> processSolutions(List<Solution> solutions, WorkspaceEntry we) {
        String title = we.getTitle();
        ComponentData componentData = getComponentData(we);

        HashSet<String> visitedTraces = new HashSet<>();
        boolean needsMultiLineMessage = solutions.size() > 1;
        if (needsMultiLineMessage) {
            LogUtils.logMessage("Unique projection(s) to '" + title + "':");
        }

        List<Solution> result = new LinkedList<>();
        for (Solution solution : solutions) {
            // Get unique projection trace
            Trace componentTrace = CompositionUtils.projectTrace(solution.getMainTrace(), componentData);
            String traceText = componentTrace.toString();
            if (!visitedTraces.contains(traceText)) {
                visitedTraces.add(traceText);
                if (needsMultiLineMessage) {
                    LogUtils.logMessage("  " + traceText);
                } else {
                    LogUtils.logMessage("Projection to '" + title + "': " + traceText);
                }

                ProjectionBuilder projectionBuilder = new ProjectionBuilder(solution, getCompositionData(), wes);
                Set<String> unexpectedlyEnabledOutputEvents = projectionBuilder.getUnexpectedlyEnabledLocalEvents(we);
                Enabledness componentEnabledness = projectionBuilder.getComponentEnabledness(we);
                result.addAll(CompositionUtils.getEnabledViolatorSolutions(componentTrace,
                        unexpectedlyEnabledOutputEvents, componentEnabledness));
            }
        }
        return result;
    }

    public boolean useWorkHierarchy() {
        return false;
    }

    @Override
    public Boolean interpret() {
        if (getOutput() == null) {
            return null;
        }
        List<Solution> solutions = getOutput().getSolutions();

        boolean predicateSatisfiable = getOutput().hasSolutions();
        boolean inversePredicate = getOutput().getVerificationParameters().isInversePredicate();
        boolean propertyHolds = predicateSatisfiable != inversePredicate;
        String message = getMessage(propertyHolds);
        if (!predicateSatisfiable) {
            OutcomeUtils.showOutcome(propertyHolds, message, isInteractive());
        } else {
            OutcomeUtils.logOutcome(propertyHolds, message);
            CompositionData compositionData = getCompositionData();
            CompositionSolutionLogger solutionLogger = new CompositionSolutionLogger(wes, compositionData, useWorkHierarchy());
            solutionLogger.write(solutions, MpsatVerificationSettings.getConformationReportStyle());
            reportSolutions(extendMessage(message), solutions);
        }
        return propertyHolds;
    }

}
