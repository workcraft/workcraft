package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.MenuOrdering;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.tools.InitialisationAnalyserTool;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public abstract class CircuitAbstractInitialisationCommand implements ScriptableCommand<Void>, MenuOrdering {

    @Override
    public final String getSection() {
        return "Initialisation";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isVisibleInMenu() {
        return false;
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            MainWindow mainWindow = framework.getMainWindow();
            GraphEditorPanel currentEditor = mainWindow.getEditor(we);
            Toolbox toolbox = currentEditor.getToolBox();
            toolbox.selectTool(toolbox.getToolInstance(InitialisationAnalyserTool.class));
        }
        return null;
    }

}
