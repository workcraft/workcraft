package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.types.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.*;

public class MpsatConformationNwayOutputHandler extends MpsatConformationOutputHandler {

    private static final String TAG_INPUT = "i";
    private static final String TAG_OUTPUT = "o";
    private static final String TAG_INTERNAL = "x";
    private static final String TAG_DUMMY = "d";
    private static final String TAG_NONE = ".";

    public enum ConformationReportStyle {
        BRIEF("Brief"),
        TABLE("Table"),
        LIST("List");

        private final String name;

        ConformationReportStyle(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class TaggedTrace extends ArrayList<Pair<String, Signal.Type>> {
    }

    private final ArrayList<WorkspaceEntry> wes;

    MpsatConformationNwayOutputHandler(ArrayList<WorkspaceEntry> wes, ExportOutput exportOutput,
            PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {

        super(wes.get(0), exportOutput, pcompOutput, mpsatOutput, settings);
        this.wes = wes;
    }

    @Override
    public Map<String, String> getSubstitutions(WorkspaceEntry we) {
        int index = wes.indexOf(we);
        return getSubstitutions(index);
    }

    @Override
    public ComponentData getCompositionData(WorkspaceEntry we) {
        int index = wes.indexOf(we);
        return getCompositionData(index);
    }

    @Override
    public StgModel getSrcStg(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        ModelEntry me = framework.cloneModel(we.getModelEntry());
        return WorkspaceUtils.getAs(me, StgModel.class);
    }

    @Override
    public void run() {
        List<MpsatSolution> solutions = getSolutions();
        boolean isConformant = solutions.isEmpty();
        String message = getMessage(!isConformant);
        if (isConformant) {
            DialogUtils.showInfo(message, TITLE);
        } else {
            LogUtils.logWarning(message);
            reportSolutions(solutions);
            Framework framework = Framework.getInstance();
            MainWindow mainWindow = framework.getMainWindow();
            for (WorkspaceEntry we : wes) {
                List<MpsatSolution> processedSolutions = processSolutions(we, solutions);
                if (!processedSolutions.isEmpty() && framework.isInGuiMode()) {
                    mainWindow.requestFocus(we);
                    String title = TITLE + " for model '" + we.getTitle() + "'";
                    String extendedMessage = extendMessage(message);
                    MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(
                            we, title, extendedMessage, processedSolutions);
                    GuiUtils.centerToParent(solutionsDialog, mainWindow);
                    solutionsDialog.setVisible(true);
                }
            }
        }
    }
    @Override
    public void reportSolutions(List<MpsatSolution> solutions) {
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.BRIEF) {
            super.reportSolutions(solutions);
        }
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.TABLE) {
            writeTables(solutions);
        }
        if (MpsatVerificationSettings.getConformationReportStyle() == ConformationReportStyle.LIST) {
            writeLists(solutions);
        }
    }

    private void writeTables(List<MpsatSolution> solutions) {
        int indentCount = 2;
        for (MpsatSolution solution : solutions) {
            LogUtils.logMessage("Violation trace of the composition: " + solution);
            writeTableHeader(indentCount, "Projected events");
            writeTableBody(solution, indentCount);
        }
    }

    private void writeTableHeader(int indentCount, String extraTitle) {
        String indent = getString(' ', indentCount);
        String prefix = "";
        for (WorkspaceEntry we : wes) {
            String title = we.getTitle();
            LogUtils.logMessage(indent + prefix + title);
            prefix += "| ";
        }
        LogUtils.logMessage(indent + prefix + extraTitle);
        String separator = getString('-', prefix.length() + extraTitle.length());
        LogUtils.logMessage(indent + separator);
    }

    private void writeTableBody(MpsatSolution solution, int indentCount) {
        Trace mainTrace = solution.getMainTrace();
        List<TaggedTrace> taggedTraces = new ArrayList<>();
        for (WorkspaceEntry we: wes) {
            TaggedTrace taggedTrace = getProjectedTaggedTrace(mainTrace, we);
            taggedTraces.add(taggedTrace);
        }

        String indent = getString(' ', indentCount);
        for (int i = 0; i < mainTrace.size(); i++) {
            List<String> tags = new ArrayList<>();
            List<String> subs = new ArrayList<>();
            for (TaggedTrace taggedTrace : taggedTraces) {
                Pair<String, Signal.Type> taggedEvent = taggedTrace.get(i);
                tags.add(getTraceEventTag(taggedEvent));
                String event = taggedEvent.getFirst();
                if (event != null) {
                    subs.add(event);
                }
            }
            String tagStr = String.join(" ", tags);
            String ref = mainTrace.get(i);
            if (subs.size() == 1) {
                ref = subs.iterator().next();
            }
            String eventStr = LabelParser.getTransitionName(ref);
            LogUtils.logMessage(indent + tagStr + " " + eventStr);
        }
    }

    private TaggedTrace getProjectedTaggedTrace(Trace trace, WorkspaceEntry we) {
        StgModel stg = getSrcStg(we);
        ComponentData data = getCompositionData(we);
        Map<String, String> substitutions = getSubstitutions(we);
        TaggedTrace result = new TaggedTrace();
        for (String ref: trace) {
            String srcRef = data.getSrcTransition(ref);
            MathNode node = null;
            if (srcRef != null) {
                if (substitutions.containsKey(srcRef)) {
                    srcRef = substitutions.get(srcRef);
                }
                node = stg.getNodeByReference(srcRef);
            }
            result.add(Pair.of(srcRef, getSignalType(node)));
        }
        return result;
    }

    private Signal.Type getSignalType(MathNode node) {
        if (node instanceof SignalTransition) {
            SignalTransition transition = (SignalTransition) node;
            return transition.getSignalType();
        }
        return null;
    }

    private String getTraceEventTag(Pair<String, Signal.Type> traceEvent) {
        if ((traceEvent != null) && (traceEvent.getFirst() != null)) {
            Signal.Type type = traceEvent.getSecond();
            if (type == null) {
                return TAG_DUMMY;
            }
            switch (type) {
            case INPUT: return TAG_INPUT;
            case OUTPUT: return TAG_OUTPUT;
            case INTERNAL: return TAG_INTERNAL;
            }
        }
        return TAG_NONE;
    }

    private void writeLists(List<MpsatSolution> solutions) {
        for (MpsatSolution solution : solutions) {
            LogUtils.logMessage("Violation trace of the composition: " + solution);
            writeList(solution);
        }
    }

    private void writeList(MpsatSolution solution) {
        Trace mainTrace = solution.getMainTrace();
        List<String> titles = new ArrayList<>();
        List<TaggedTrace> taggedTraces = new ArrayList<>();
        for (WorkspaceEntry we: wes) {
            titles.add(we.getTitle());
            taggedTraces.add(getProjectedTaggedTrace(mainTrace, we));
        }

        for (int i = 0; i < mainTrace.size(); i++) {
            List<String> inputRefs = new ArrayList<>();
            List<String> outputRefs = new ArrayList<>();
            List<String> internalRefs = new ArrayList<>();
            String event = mainTrace.get(i);
            for (int j = 0; j < titles.size(); j++) {
                String title = titles.get(j);
                TaggedTrace taggedTrace = taggedTraces.get(j);
                Pair<String, Signal.Type> taggedEvent = taggedTrace.get(i);
                String ref = taggedEvent.getFirst();
                if (ref != null) {
                    Signal.Type type = taggedEvent.getSecond();
                    if ((type == null) || (type == Signal.Type.INTERNAL)) {
                        internalRefs.add(title);
                        event = ref;
                    }
                    if (type == Signal.Type.INPUT) {
                        inputRefs.add(title);
                    }
                    if (type == Signal.Type.OUTPUT) {
                        outputRefs.add(title);
                    }
                }
            }
            String eventStr = LabelParser.getTransitionName(event);
            String inputStr = String.join(", ", inputRefs);
            String outputStr = String.join(", ", outputRefs);
            String internalStr = String.join(", ", internalRefs);
            if (!internalStr.isEmpty()) {
                LogUtils.logMessage("  " + eventStr + " : " + internalStr);
            } else {
                LogUtils.logMessage("  " + eventStr + " : " + outputStr + " -> " + inputStr);
            }
        }
    }

    private String getString(char symbol, int count) {
        char[] buf = new char[count];
        Arrays.fill(buf, symbol);
        return String.valueOf(buf);
    }

}
