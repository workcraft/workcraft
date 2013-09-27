package org.workcraft.gui.graph.tools;

import javax.swing.Icon;

import org.workcraft.util.GUI;

public class ExpressionTool extends AbstractTool {

	@Override
	public String getLabel() {
		return "Expressions";
	}

	@Override
	public Decorator getDecorator() {
		return Decorator.Empty.INSTANCE;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/expression-tool.svg");
	}

}
