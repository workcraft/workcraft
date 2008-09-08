package org.workcraft.framework.workspace;
import java.util.EventListener;

import org.workcraft.dom.AbstractGraphModel;


public interface WorkspaceListener extends EventListener {
	public void workspaceChanged();
	public void workspaceSaved();
}