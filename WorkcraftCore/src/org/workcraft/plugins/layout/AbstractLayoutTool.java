package org.workcraft.plugins.layout;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

abstract public class AbstractLayoutTool implements Tool {

	@Override
	public String getSection() {
		return "Graph layout";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, VisualModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		we.saveMemento();
		VisualModel model = WorkspaceUtils.getAs(we, VisualModel.class);
		layout(model);
		MainWindow mainWindow = Framework.getInstance().getMainWindow();
		if (mainWindow != null) {
			GraphEditor editor = mainWindow.getCurrentEditor();
			if (editor != null) {
				editor.zoomFit();
			}
		}
	}

	abstract public void layout(VisualModel model);

}
