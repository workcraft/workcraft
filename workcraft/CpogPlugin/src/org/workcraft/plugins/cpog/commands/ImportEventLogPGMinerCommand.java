package org.workcraft.plugins.cpog.commands;

import org.workcraft.Framework;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.gui.PGMinerImportDialog;
import org.workcraft.plugins.cpog.tasks.PGMinerResultHandler;
import org.workcraft.plugins.cpog.tasks.PGMinerTask;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.Scanner;

public class ImportEventLogPGMinerCommand extends AbstractPGMinerCommand {

    @Override
    public String getDisplayName() {
        return "Import an event log";
    }

    @Override
    public void run(WorkspaceEntry we) {
        PGMinerImportDialog dialog = new PGMinerImportDialog();
        if (dialog.reveal()) {
            File inputFile = new File(dialog.getFilePath());
            final Framework framework = Framework.getInstance();
            final Toolbox toolbox = framework.getMainWindow().getEditor(we).getToolBox();
            final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);
            VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
                we.cancelMemento();
            }
        }
    }

}
