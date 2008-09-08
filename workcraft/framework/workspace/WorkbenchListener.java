package org.workcraft.framework.workspace;

import java.util.EventListener;

import org.workcraft.dom.AbstractGraphModel;

public interface WorkbenchListener extends EventListener {
	public void documentLoaded(AbstractGraphModel doc);
}