package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.gui.ReachibilityDialog;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ReachabilityOutputHandler implements Runnable {

    protected static final String TITLE = "Verification results";

    private final WorkspaceEntry we;
    private ExportOutput exportOutput;
    private final PcompOutput pcompOutput;
    private final VerificationOutput mpsatOutput;
    private final VerificationParameters settings;

    private CompositionData compositionData = null;

    ReachabilityOutputHandler(WorkspaceEntry we, VerificationOutput mpsatOutput, VerificationParameters settings) {
        this(we, null, null, mpsatOutput, settings);
    }

    ReachabilityOutputHandler(WorkspaceEntry we, ExportOutput exportOutput, PcompOutput pcompOutput, VerificationOutput mpsatOutput, VerificationParameters settings) {
        this.we = we;
        this.exportOutput = exportOutput;
        this.pcompOutput = pcompOutput;
        this.mpsatOutput = mpsatOutput;
        this.settings = settings;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    public ExportOutput getExportOutput() {
        return exportOutput;
    }

    public PcompOutput getPcompOutput() {
        return pcompOutput;
    }

    public VerificationOutput getMpsatOutput() {
        return mpsatOutput;
    }

    public VerificationParameters getSettings() {
        return settings;
    }

    public List<Solution> getSolutions() {
        VerificationOutputParser mrp = new VerificationOutputParser(getMpsatOutput());
        return mrp.getSolutions();
    }

    public void reportSolutions(List<Solution> solutions) {
    }

    public List<Solution> processSolutions(WorkspaceEntry we, List<Solution> solutions) {
        List<Solution> result = new LinkedList<>();
        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);
        for (Solution solution: solutions) {
            Trace mainTrace = getProjectedTrace(solution.getMainTrace(), data, substitutions);
            Trace branchTrace = getProjectedTrace(solution.getBranchTrace(), data, substitutions);
            String comment = solution.getComment();
            Solution processedSolution = new Solution(mainTrace, branchTrace, comment);
            result.add(processedSolution);
        }
        return result;
    }

    public Trace getProjectedTrace(Trace trace, ComponentData data, Map<String, String> substitutions) {
        if ((trace == null) || trace.isEmpty() || (data == null)) {
            return trace;
        }
        Trace result = new Trace();
        for (String ref: trace) {
            String srcRef = data.getSrcTransition(ref);
            if (srcRef != null) {
                if (substitutions.containsKey(srcRef)) {
                    srcRef = substitutions.get(srcRef);
                }
                result.add(srcRef);
            }
        }
        return result;
    }

    public Map<String, String> getSubstitutions(WorkspaceEntry we) {
        return getSubstitutions(0);
    }

    public Map<String, String> getSubstitutions(int index) {
        if (getExportOutput() instanceof MultiSubExportOutput) {
            MultiSubExportOutput exportOutput = (MultiSubExportOutput) getExportOutput();
            return exportOutput.getSubstitutions(index);
        }
        return new HashMap<>();
    }

    public ComponentData getCompositionData(WorkspaceEntry we) {
        return getCompositionData(0);
    }

    public ComponentData getCompositionData(int index) {
        if (compositionData == null) {
            if (getPcompOutput() != null) {
                File detailFile = getPcompOutput().getDetailFile();
                try {
                    compositionData = new CompositionData(detailFile);
                } catch (FileNotFoundException e) {
                }
            }
        }
        return compositionData == null ? null : compositionData.getComponentData(index);
    }

    public StgModel getSrcStg(WorkspaceEntry we) {
        ComponentData data = getCompositionData(we);
        if (data == null) {
            return getMpsatOutput().getInputStg();
        }
        File file = new File(data.getFileName());
        if ((file != null) && file.exists()) {
            return StgUtils.importStg(file);
        }
        return null;
    }

    public String getMessage(boolean isSatisfiable) {
        String propertyName = "Property";
        if ((getSettings().getName() != null) && !getSettings().getName().isEmpty()) {
            propertyName = getSettings().getName();
        }
        boolean inversePredicate = getSettings().getInversePredicate();
        String propertyStatus = isSatisfiable == inversePredicate ? " is violated." : " holds.";
        return propertyName + propertyStatus;
    }

    public String extendMessage(String message) {
        String traceCharacteristic = getSettings().getInversePredicate() ? "problematic" : "sought";
        String traceInfo = "&#160;Trace(s) leading to the " + traceCharacteristic + " state(s):<br><br>";
        return "<html><br>&#160;" + message + "<br><br>" + traceInfo + "</html>";
    }

    @Override
    public void run() {
        List<Solution> solutions = getSolutions();
        boolean isSatisfiable = MpsatUtils.hasTraces(solutions);
        String message = getMessage(isSatisfiable);
        if (!isSatisfiable) {
            if (getSettings().getInversePredicate()) {
                DialogUtils.showInfo(message, TITLE);
            } else {
                DialogUtils.showWarning(message, TITLE);
            }
        } else {
            LogUtils.logWarning(message);
            reportSolutions(solutions);
            List<Solution> processedSolutions = processSolutions(getWorkspaceEntry(), solutions);
            Framework framework = Framework.getInstance();
            if (framework.isInGuiMode()) {
                message = extendMessage(message);
                ReachibilityDialog solutionsDialog = new ReachibilityDialog(
                        getWorkspaceEntry(), TITLE, message, processedSolutions);
                MainWindow mainWindow = framework.getMainWindow();
                GuiUtils.centerToParent(solutionsDialog, mainWindow);
                solutionsDialog.setVisible(true);
            }
        }
    }

}
