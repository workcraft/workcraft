package org.workcraft.framework;

import org.workcraft.gui.edit.tools.GraphEditorTool;

public interface CustomToolsProvider {
	public Iterable<GraphEditorTool> getTools();
}
