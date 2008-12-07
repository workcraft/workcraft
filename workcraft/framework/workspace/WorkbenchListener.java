package org.workcraft.framework.workspace;

import java.util.EventListener;

import org.workcraft.dom.MathModel;

public interface WorkbenchListener extends EventListener {
	public void documentLoaded(MathModel doc);
}