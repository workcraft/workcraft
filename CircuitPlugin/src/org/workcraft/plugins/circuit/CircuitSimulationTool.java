package org.workcraft.plugins.circuit;

import java.awt.event.KeyEvent;

import javax.swing.Icon;

import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.GUI;


public class CircuitSimulationTool extends AbstractTool {

	@Override
	public String getLabel() {
		return "Simulation";
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_M;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/start-green.svg");
	}

	@Override
	public void activated(GraphEditor editor) {
		STG stg = new STG();
	}

}
