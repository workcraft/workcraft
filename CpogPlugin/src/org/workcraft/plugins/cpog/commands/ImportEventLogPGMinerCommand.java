package org.workcraft.plugins.cpog.commands;

import java.io.File;
import java.util.Scanner;

import org.workcraft.Framework;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.gui.PGMinerImportDialog;
import org.workcraft.plugins.cpog.tasks.PGMinerResultHandler;
import org.workcraft.plugins.cpog.tasks.PGMinerTask;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ImportEventLogPGMinerCommand implements Command {

    boolean split = false;
    PGMinerImportDialog dialog;

    @Override
    public String getSection() {
        return "! Process Mining";
    }

    @Override
    public String getDisplayName() {
        return "Import an event log";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

    public File getInputFile(WorkspaceEntry we) {
        dialog = new PGMinerImportDialog();
        dialog.setVisible(true);
        if (!dialog.getCanImport()) {
            return null;
        }
        return new File(dialog.getFilePath());
    }

    @Override
    public void run(WorkspaceEntry we) {
        File inputFile = getInputFile(we);
        final Framework framework = Framework.getInstance();
        final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
        final Toolbox toolbox = editor.getToolBox();
        final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);
        VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);
        try {
            if (inputFile != null) {
                if (dialog.getExtractConcurrency()) {
                    TaskManager taskManager = framework.getTaskManager();
                    PGMinerTask task = new PGMinerTask(inputFile, dialog.getSplit());
                    PGMinerResultHandler result = new PGMinerResultHandler(visualCpog, we, false);
                    taskManager.queue(task, "PGMiner", result);
                } else {
                    Scanner k = new Scanner(inputFile);
                    int i = 0;
                    double yPos = tool.getLowestVertex(visualCpog).getY() + 3;
                    we.captureMemento();
                    while (k.hasNext()) {
                        String line = k.nextLine();
                        tool.insertEventLog(visualCpog, i++, line.split(" "), yPos);
                        yPos = yPos + 5;
                    }
                    k.close();
                    we.saveMemento();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            we.cancelMemento();
        }
    }

}
