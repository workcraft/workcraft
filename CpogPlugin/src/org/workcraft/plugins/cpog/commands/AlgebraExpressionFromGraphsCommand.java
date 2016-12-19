package org.workcraft.plugins.cpog.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.gui.AlgebraExportDialog;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class AlgebraExpressionFromGraphsCommand implements Command {

    private static final String DIALOG_SAVE_FILE = "Save file";
    private static final String DIALOG_EXPRESSION_EXPORT_ERROR = "Expression export error";

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

    @Override
    public String getSection() {
        return "! Algebra";
    }

    @Override
    public String getDisplayName() {
        return "Get expression from graphs (selected or all)";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final GraphEditorPanel editor = mainWindow.getCurrentEditor();
        final ToolboxPanel toolbox = editor.getToolBox();
        final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);

        VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);
        String exp = CpogParsingTool.getExpressionFromGraph(visualCpog);
        AlgebraExportDialog dialog = new AlgebraExportDialog();

        if (exp == "") {
            return;
        }
        dialog.setVisible(true);
        if (!dialog.getOK()) {
            return;
        }
        if (dialog.getPaste()) {
            tool.setExpressionText(exp);
            return;
        }
        if (dialog.getExport()) {
            String filePath = dialog.getFilePath();
            if (filePath.compareTo(" ") == 0 || filePath == "") {
                JOptionPane.showMessageDialog(mainWindow,
                        "No export file has been given",
                        DIALOG_EXPRESSION_EXPORT_ERROR, JOptionPane.ERROR_MESSAGE);
                return;
            }
            File file = new File(filePath);
            if (file.exists()) {
                if (JOptionPane.showConfirmDialog(mainWindow,
                            "The file '" + file.getName() + "' already exists.\n" + "Overwrite it?",
                            DIALOG_SAVE_FILE, JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            PrintStream expressions;
            try {
                expressions = new PrintStream(file);
                expressions.print(exp);
                expressions.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "No export selection was made",
                    DIALOG_EXPRESSION_EXPORT_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

}
