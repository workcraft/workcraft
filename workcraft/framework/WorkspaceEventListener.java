package org.workcraft.framework;
import java.util.EventListener;


public interface WorkspaceEventListener extends EventListener {
	public void documentOpened(Document doc);
	// public void documentClosing (Document doc);
	public void workspaceUpdated();
}