package org.workcraft.plugins.cpog.scenco;

import java.awt.Window;

import javax.swing.JOptionPane;

import org.workcraft.Command;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tasks.ScencoExternalToolTask;
import org.workcraft.plugins.cpog.tasks.ScencoResultHandler;
import org.workcraft.plugins.cpog.tasks.ScencoSolver;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractScencoCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

    @Override
    public String getSection() {
        return "!Encoding";
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        if (!CpogParsingTool.hasEnoughScenarios(we)) {
            JOptionPane.showMessageDialog(mainWindow, ScencoSolver.MSG_NOT_ENOUGH_SCENARIOS,
                    ScencoSolver.ACCESS_SCENCO_ERROR, JOptionPane.ERROR_MESSAGE);
        } else if (CpogParsingTool.hasTooScenarios(we)) {
            JOptionPane.showMessageDialog(mainWindow, ScencoSolver.MSG_TOO_MANY_SCENARIOS,
                    ScencoSolver.ACCESS_SCENCO_ERROR, JOptionPane.ERROR_MESSAGE);
        } else {
            AbstractScencoDialog dialog = createDialog(mainWindow, we);
            GUI.centerToParent(dialog, mainWindow);
            dialog.setVisible(true);
            if (dialog.isDone()) {
                final ScencoSolver solver = new ScencoSolver(dialog.getSettings(), we);
                final ScencoExternalToolTask scencoTask = new ScencoExternalToolTask(we, solver);
                final ScencoResultHandler resultScenco = new ScencoResultHandler(scencoTask);
                final TaskManager taskManager = framework.getTaskManager();
                taskManager.queue(scencoTask, dialog.getTitle(), resultScenco);
            }
        }
    }

    public abstract AbstractScencoDialog createDialog(Window owner, WorkspaceEntry we);

}
