package org.workcraft.plugins.cpog.scenco;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tasks.ScencoExternalToolTask;
import org.workcraft.plugins.cpog.tasks.ScencoResultHandler;
import org.workcraft.plugins.cpog.tasks.ScencoSolver;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.CommandUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;

public abstract class AbstractScencoCommand implements Command {

    public static final String SECTION_TITLE = CommandUtils.makePromotedSectionTitle("Encoding", 5);

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

    @Override
    public String getSection() {
        return SECTION_TITLE;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (!CpogParsingTool.hasEnoughScenarios(we)) {
            DialogUtils.showError(ScencoSolver.MSG_NOT_ENOUGH_SCENARIOS);
        } else if (CpogParsingTool.hasTooScenarios(we)) {
            DialogUtils.showError(ScencoSolver.MSG_TOO_MANY_SCENARIOS);
        } else {
            AbstractScencoDialog dialog = createDialog(mainWindow, we);
            if (dialog.reveal()) {
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
