package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

class MpsatReachabilityOutputHandler implements Runnable {

    public static final String TITLE = "Verification results";
    private final WorkspaceEntry we;
    private final PcompOutput pcompOutput;
    private final MpsatOutput mpsatOutput;
    private final MpsatParameters settings;

    MpsatReachabilityOutputHandler(WorkspaceEntry we, MpsatOutput mpsatOutput, MpsatParameters settings) {
        this(we, null, mpsatOutput, settings);
    }

    MpsatReachabilityOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        this.we = we;
        this.pcompOutput = pcompOutput;
        this.mpsatOutput = mpsatOutput;
        this.settings = settings;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    public PcompOutput getPcompOutput() {
        return pcompOutput;
    }

    public MpsatOutput getMpsatOutput() {
        return mpsatOutput;
    }

    public MpsatParameters getSettings() {
        return settings;
    }

    public List<MpsatSolution> getSolutions() {
        List<MpsatSolution> solutions = new LinkedList<>();
        MpsatOutoutParser mrp = new MpsatOutoutParser(getMpsatOutput());
        for (MpsatSolution solution: mrp.getSolutions()) {
            solutions.add(processSolution(solution));
        }
        return solutions;
    }

    public MpsatSolution processSolution(MpsatSolution solution) {
        Trace mainTrace = getProjectedTrace(solution.getMainTrace());
        Trace branchTrace = getProjectedTrace(solution.getBranchTrace());
        String comment = solution.getComment();
        return new MpsatSolution(mainTrace, branchTrace, comment);
    }

    public Trace getProjectedTrace(Trace trace) {
        if ((trace == null) || (getPcompOutput() == null)) {
            return trace;
        }
        Trace result = new Trace();
        File detailFile = getPcompOutput().getDetailFile();
        try {
            CompositionData compositionData = new CompositionData(detailFile);
            ComponentData componentData = compositionData.getComponentData(0);
            for (String ref: trace) {
                String srcRef = componentData.getSrcTransition(ref);
                if (srcRef != null) {
                    result.add(srcRef);
                }
            }
        } catch (FileNotFoundException e) {
            return trace;
        }
        return result;
    }

    public StgModel getSrcStg() {
        File file = null;
        if (getPcompOutput() != null) {
            File[] inputFiles = getPcompOutput().getInputFiles();
            if ((inputFiles != null) && (inputFiles.length > 0)) {
                file = inputFiles[0];
            }
        }
        return StgUtils.importStg(file);
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
        List<MpsatSolution> solutions = getSolutions();
        String message = getMessage(!solutions.isEmpty());
        Framework framework = Framework.getInstance();
        if (!MpsatUtils.hasTraces(solutions)) {
            DialogUtils.showInfo(message, TITLE);
        } else if (framework.isInGuiMode()) {
            message = extendMessage(message);
            MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(we, TITLE, message, solutions);
            MainWindow mainWindow = framework.getMainWindow();
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        }
    }

}
