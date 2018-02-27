package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
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

    private static final String TITLE = "Verification results";

    private final WorkspaceEntry we;
    private final PcompOutput pcompOutput;
    private final MpsatOutput mpsatOutput;
    private final MpsatParameters settings;

    private final HashMap<String, StgModel> srcStgs = new HashMap<>();
    private CompositionData compositionData = null;

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
        return getSolutions(0);
    }

    public List<MpsatSolution> getSolutions(int index) {
        List<MpsatSolution> solutions = new LinkedList<>();
        MpsatOutoutParser mrp = new MpsatOutoutParser(getMpsatOutput());
        ComponentData data = getCompositionData(index);
        for (MpsatSolution solution: mrp.getSolutions()) {
            MpsatSolution processedSolution = processSolution(solution, data);
            if (processedSolution != null) {
                solutions.add(processedSolution);
            }
        }
        return solutions;
    }

    public MpsatSolution processSolution(MpsatSolution solution, ComponentData data) {
        Trace mainTrace = getProjectedTrace(solution.getMainTrace(), data);
        Trace branchTrace = getProjectedTrace(solution.getBranchTrace(), data);
        String comment = solution.getComment();
        return new MpsatSolution(mainTrace, branchTrace, comment);
    }

    public Trace getProjectedTrace(Trace trace, ComponentData data) {
        if ((trace == null) || trace.isEmpty() || (data == null)) {
            return trace;
        }
        Trace result = new Trace();
        for (String ref: trace) {
            String srcRef = data.getSrcTransition(ref);
            if (srcRef != null) {
                result.add(srcRef);
            }
        }
        return result;
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

    public StgModel getSrcStg(ComponentData data) {
        if (data == null) {
            return null;
        }
        if (!srcStgs.containsKey(data.getFileName())) {
            File file = new File(data.getFileName());
            if ((file != null) && file.exists()) {
                StgModel srcStg = StgUtils.importStg(file);
                srcStgs.put(data.getFileName(), srcStg);
            }
        }
        return srcStgs.get(data.getFileName());
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
            MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(getWorkspaceEntry(), TITLE, message, solutions);
            MainWindow mainWindow = framework.getMainWindow();
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        }
    }

}
