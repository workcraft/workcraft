/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.graph.tools;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.NodeFactory;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.TransformHelper;
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
		{
			AffineTransform transform = TransformHelper.getTransform(model.getRoot(), vc);
			Point2D transformed = new Point2D.Double();
			transform.transform(where, transformed);
			MovableHelper.translate((Movable)vc, transformed.getX(), transformed.getY());
		}
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
