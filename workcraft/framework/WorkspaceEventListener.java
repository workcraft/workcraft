package org.workcraft.framework;
import java.util.EventListener;

import org.workcraft.dom.AbstractGraphModel;


public interface WorkspaceEventListener extends EventListener {
	public void documentOpened(AbstractGraphModel doc);
	// public void documentClosing (Document doc);
	public void workspaceUpdated();
}