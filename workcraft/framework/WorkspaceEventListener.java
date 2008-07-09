package org.workcraft.framework;
import java.util.EventListener;

import org.workcraft.dom.WorkDocument;


public interface WorkspaceEventListener extends EventListener {
	public void documentOpened(WorkDocument doc);
	// public void documentClosing (Document doc);
	public void workspaceUpdated();
}