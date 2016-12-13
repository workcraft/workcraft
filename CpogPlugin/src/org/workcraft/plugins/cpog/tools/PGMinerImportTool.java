package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.util.Scanner;

import org.workcraft.Framework;
import org.workcraft.Command;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.gui.PGMinerImportDialog;
import org.workcraft.plugins.cpog.tasks.PGMinerResultHandler;
import org.workcraft.plugins.cpog.tasks.PGMinerTask;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PGMinerImportTool implements Command {

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
    public boolean isApplicableTo(ModelEntry me) {
        return me.getVisualModel() instanceof VisualCpog;
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
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        File inputFile = getInputFile(we);
        final Framework framework = Framework.getInstance();
        final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
        final ToolboxPanel toolbox = editor.getToolBox();
        final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);

        try {
            if (inputFile != null) {
                if (dialog.getExtractConcurrency()) {
                    PGMinerTask task = new PGMinerTask(inputFile, dialog.getSplit());

                    PGMinerResultHandler result = new PGMinerResultHandler((VisualCpog) we.getModelEntry().getVisualModel(), we, false);
                    framework.getTaskManager().queue(task, "PGMiner", result);
                } else {
                    Scanner k;
                    k = new Scanner(inputFile);
                    int i = 0;
                    double yPos = tool.getLowestVertex((VisualCpog) editor.getWorkspaceEntry().getModelEntry().getVisualModel()).getY() + 3;
                    editor.getWorkspaceEntry().captureMemento();
                    while (k.hasNext()) {
                        String line = k.nextLine();
                        tool.insertEventLog((VisualCpog) editor.getWorkspaceEntry().getModelEntry().getVisualModel(), i++, line.split(" "), yPos);
                        yPos = yPos + 5;
                    }
                    k.close();
                    editor.getWorkspaceEntry().saveMemento();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            editor.getWorkspaceEntry().cancelMemento();
        }
        return we;
    }

}
