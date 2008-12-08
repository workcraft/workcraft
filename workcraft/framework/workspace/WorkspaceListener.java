package org.workcraft.framework.workspace;
import java.util.EventListener;


public interface WorkspaceListener extends EventListener {
	//public void workspaceChanged();
	public void workspaceSaved();
	public void entryAdded(WorkspaceEntry we);
	public void entryRemoved(WorkspaceEntry we);
	public void modelLoaded(WorkspaceEntry we);
	public void entryChanged(WorkspaceEntry we);
}