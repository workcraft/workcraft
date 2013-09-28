package org.workcraft.gui.graph.tools;

import java.awt.event.KeyEvent;

import javax.swing.Icon;

import org.workcraft.util.GUI;

public class ExpressionTool extends SelectionTool {

	@Override
	public String getLabel() {
		return "Expressions";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_E;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/expression-tool.svg");
	}

}
