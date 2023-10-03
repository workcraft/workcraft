package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.stg.tools.EncodingConflict;
import org.workcraft.plugins.stg.tools.EncodingConflictAnalyserTool;
import org.workcraft.shared.ColorGenerator;
import org.workcraft.tasks.AbstractOutputInterpreter;
import org.workcraft.traces.Solution;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class EncodingConflictOutputHandler extends AbstractOutputInterpreter<MpsatOutput, Boolean> {

    private static final Pattern SIGNAL_PATTERN = Pattern.compile(
            "CSC conflict for signal (.+)");

    private final ColorGenerator colorGenerator = new ColorGenerator(ColorUtils.getHsbPalette(
            new float[]{0.45f, 0.15f, 0.70f, 0.25f, 0.05f, 0.80f, 0.55f, 0.20f, 075f, 0.50f},
            new float[]{0.30f}, new float[]{0.9f, 0.7f, 0.5f}));

    EncodingConflictOutputHandler(WorkspaceEntry we, MpsatOutput output, boolean interactive) {
        super(we, output, interactive);
    }

    @Override
    public Boolean interpret() {
        if (getOutput() == null) {
            return null;
        }
        boolean propertyHolds = !getOutput().hasSolutions();
        if (propertyHolds) {
            String msg = "No encoding conflicts.";
            if (isInteractive()) {
                DialogUtils.showInfo(msg, "Verification results");
            } else {
                LogUtils.logInfo(msg);
            }
        } else {
            List<Solution> solutions = getOutput().getSolutions();
            ArrayList<EncodingConflict> encodingConflicts = getOrderedUniqueConflicts(solutions);
            if (MpsatVerificationSettings.getDebugCores()) {
                for (EncodingConflict encodingConflict : encodingConflicts) {
                    LogUtils.logMessage(encodingConflict.getDescription());
                }
            }
            if (isInteractive()) {
                final MainWindow mainWindow = Framework.getInstance().getMainWindow();
                final Toolbox toolbox = mainWindow.getToolbox(getWorkspaceEntry());
                final EncodingConflictAnalyserTool tool = toolbox.getToolInstance(EncodingConflictAnalyserTool.class);
                toolbox.selectTool(tool);
                tool.setEncodingConflicts(encodingConflicts);
            }
        }
        return propertyHolds;
    }

    private ArrayList<EncodingConflict> getOrderedUniqueConflicts(List<Solution> solutions) {
        ArrayList<EncodingConflict> result = new ArrayList<>();
        Set<Set<String>> uniqueCores = new HashSet<>();
        for (Solution solution: solutions) {
            String comment = solution.getComment();
            Matcher matcher = SIGNAL_PATTERN.matcher(comment);
            EncodingConflict encodingConflict = new EncodingConflict(solution.getMainTrace(), solution.getBranchTrace(),
                    matcher.find() ? matcher.group(1) : null);

            Set<String> conflictCore = encodingConflict.getCore();
            if (!uniqueCores.contains(conflictCore)) {
                encodingConflict.setColor(colorGenerator.updateColor());
                result.add(encodingConflict);
                uniqueCores.add(conflictCore);
            }
        }
        result.sort((c1, c2) -> {
            if (c1.getCore().size() > c2.getCore().size()) return 1;
            if (c1.getCore().size() < c2.getCore().size()) return -1;
            return Integer.compare(c1.getCoreAsString().length(), c2.getCoreAsString().length());
        });
        return result;
    }

}
