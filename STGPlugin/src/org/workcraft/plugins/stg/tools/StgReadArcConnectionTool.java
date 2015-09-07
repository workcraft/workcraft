package org.workcraft.plugins.stg.tools;

import java.awt.event.KeyEvent;

import javax.swing.Icon;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.VisualReadArc;
import org.workcraft.util.GUI;

public class StgReadArcConnectionTool extends ConnectionTool {

	public StgReadArcConnectionTool() {
		super(true, false);
	}

	@Override
	public boolean isConnectable(Node node) {
		return ((node instanceof VisualPlace) || (node instanceof VisualTransition));
	}

	@Override
	public VisualConnection createDefaultTemplateNode() {
		return new VisualReadArc();
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/tool-readarc.svg");
	}

	@Override
	public String getLabel() {
		return "Read-arc";
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_R;
	}

}
