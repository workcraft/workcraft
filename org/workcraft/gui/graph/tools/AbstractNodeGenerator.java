package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.NodeFactory;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;

public abstract class AbstractNodeGenerator implements NodeGenerator {

	public interface MathNodeGenerator
	{
		public MathNode createNode();
		public String getLabel();
		public int getHotKeyCode();
	}

	@Override
	public void generate(VisualModel model, Point2D where) throws NodeCreationException {
		MathNode mn = createMathNode();
		model.getMathModel().add(mn);

		VisualNode vc = NodeFactory.createVisualComponent(mn);
		model.getCurrentLevel().add(vc);

		if (vc instanceof Movable)
			MovableHelper.translate((Movable)vc, where.getX(), where.getY());
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getText() {
		return "Click to create a " + getLabel();
	}

	@Override
	public int getHotKeyCode() {
		return -1; // undefined hotkey
	}

	protected abstract MathNode createMathNode() throws NodeCreationException;
}
