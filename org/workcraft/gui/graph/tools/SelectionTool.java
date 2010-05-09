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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Icon;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Colorisable;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.SelectionColoriser;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;

public class SelectionTool extends AbstractTool {
	private static final int DRAG_NONE = 0;
	private static final int DRAG_MOVE = 1;
	private static final int DRAG_SELECT = 2;

	private static final int SELECTION_NONE = 0;
	private static final int SELECTION_ADD = 1;
	private static final int SELECTION_REMOVE = 2;
	private static final int SELECTION_REPLACE = 3;


	protected Color selectionBorderColor = new Color(200, 200, 200);
	protected Color selectionFillColor = new Color(99, 130, 191, 32);
	protected Color grayOutColor = Color.LIGHT_GRAY;

	private SelectionColoriser coloriser;

	private int drag = DRAG_NONE;
	private boolean notClick = false;

	private Point2D snapOffset;

	private LinkedList<Node> selected = new LinkedList<Node>();
	private int selectionMode;

	private Rectangle2D selectionBox = null;

	@Override
	public void activated(GraphEditor editor) {
		coloriser = new SelectionColoriser(editor.getModel());
		coloriser.activate();
	}

	@Override
	public void deactivated(GraphEditor editor) {
		coloriser.deactivate();
	}

	@Override
	public boolean isDragging() {
		return drag!=DRAG_NONE;
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {

		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			if (node != null)
			{
				switch(e.getKeyModifiers()) {
					case 0:
						e.getModel().select(node);
						break;
					case MouseEvent.SHIFT_DOWN_MASK:
						e.getModel().addToSelection(node);
						break;
					case MouseEvent.CTRL_DOWN_MASK:
						e.getModel().removeFromSelection(node);
						break;
				}
			} else {
				if (e.getKeyModifiers()==0)
					e.getModel().selectNone();
			}
		}
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			Point2D p1 = e.getEditor().snap(new Point2D.Double(e.getPrevPosition().getX()+snapOffset.getX(), e.getPrevPosition().getY()+snapOffset.getY()));
			Point2D p2 = e.getEditor().snap(new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY()));
			offsetSelection(e, p2.getX()-p1.getX(), p2.getY()-p1.getY());
		}
		else if(drag==DRAG_SELECT) {
			SelectionColoriser.uncolorise(selected);
			selected.clear();
			selected.addAll(model.boxHitTest(e.getStartPosition(), e.getPosition()));

			SelectionColoriser.colorise(e.getModel().getSelection());
			if (selectionMode == SELECTION_ADD || selectionMode == SELECTION_REPLACE) {
				SelectionColoriser.colorise(selected);
			} else {
				SelectionColoriser.uncolorise(selected);
			}

			selectionBox = selectionRect(e.getStartPosition(), e.getPosition());

			e.getEditor().repaint();
		}
	}

	@Override
	public void startDrag(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(e.getButtonModifiers()==MouseEvent.BUTTON1_DOWN_MASK) {
			VisualNode hitNode = (VisualNode) HitMan.hitTestForSelection(e.getStartPosition(), model);

			if (hitNode == null) {
				// hit nothing, so start select-drag

				switch(e.getKeyModifiers()) {
					case 0:
						selectionMode = SELECTION_REPLACE;
						break;
					case MouseEvent.CTRL_DOWN_MASK:
						selectionMode = SELECTION_REMOVE;
						break;
					case MouseEvent.SHIFT_DOWN_MASK:
						selectionMode = SELECTION_ADD;
						break;
					default:
						selectionMode = SELECTION_NONE;
				}

				if(selectionMode!=SELECTION_NONE) {
					// selection will not actually be changed until drag completes
					drag = DRAG_SELECT;
					selected.clear();

					if(selectionMode==SELECTION_REPLACE)
						model.selectNone();
					else
						selected.addAll(model.getSelection());
				}

			} else {
				// hit something

				if(e.getKeyModifiers()==0 && hitNode instanceof Movable) {
					// mouse down without modifiers, begin move-drag
					drag = DRAG_MOVE;

					if(hitNode!=null && !model.getSelection().contains(hitNode))
						e.getModel().select(hitNode);

					Movable node = (Movable) hitNode;
					Point2D pos = new Point2D.Double(node.getTransform().getTranslateX(), node.getTransform().getTranslateY());
					Point2D pSnap = e.getEditor().snap(pos);
					offsetSelection(e, pSnap.getX()-pos.getX(), pSnap.getY()-pos.getY());
					snapOffset = new Point2D.Double(pSnap.getX()-e.getStartPosition().getX(), pSnap.getY()-e.getStartPosition().getY());

				}
				// do nothing if pressed on a node with modifiers

			}
		}
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON3) {

			if(isDragging()) {
				cancelDrag(e);
				e.getEditor().repaint();
				notClick = true; // TODO left click still generated but it should not
			}
			else {
				notClick = false;
			}
		}
	}

	@Override
	public void finishDrag(GraphEditorMouseEvent e) {
		if (drag == DRAG_SELECT)
		{
			if (selectionMode == SELECTION_REPLACE)
				e.getModel().select(selected);
			else if (selectionMode == SELECTION_ADD)
				e.getModel().addToSelection(selected);
			else if (selectionMode == SELECTION_REMOVE)
				e.getModel().removeFromSelection(selected);
			selectionBox = null;
		}
		drag = DRAG_NONE;

		e.getEditor().repaint();
	}


	private void cancelDrag(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			Point2D p1 = e.getEditor().snap(new Point2D.Double(e.getStartPosition().getX()+snapOffset.getX(), e.getStartPosition().getY()+snapOffset.getY()));
			Point2D p2 = e.getEditor().snap(new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY()));
			offsetSelection(e, p1.getX()-p2.getX(), p1.getY()-p2.getY());
		}
		else if(drag == DRAG_SELECT) {
			SelectionColoriser.uncolorise(selected);
			SelectionColoriser.colorise(model.getSelection());
			selected.clear();
			selectionBox = null;
		}
		drag = DRAG_NONE;

		e.getEditor().repaint();
	}

	@Override
	public void mouseReleased(GraphEditorMouseEvent e) {
		// do nothing
	}

	private void grayOutNotActive(VisualModel model)
	{
		Node root = model.getRoot();
		if (root instanceof Colorisable)
			((Colorisable)root).setColorisation(grayOutColor);

		coloriser.update();
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			e.getModel().deleteSelection();
			e.getEditor().repaint();
		}

		if (!e.isCtrlDown())
		{
			if (!e.isShiftDown()) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_PAGE_UP:
					currentLevelUp(e.getModel());
					break;
				case KeyEvent.VK_PAGE_DOWN:
					currentLevelDown(e.getModel());
					break;
				case KeyEvent.VK_OPEN_BRACKET:
					VisualModelTransformer.rotateSelection(e.getEditor(), e.getModel(),-Math.PI/2);
					break;
				case KeyEvent.VK_CLOSE_BRACKET:
					VisualModelTransformer.rotateSelection(e.getEditor(), e.getModel(),Math.PI/2);
					break;
				case KeyEvent.VK_LEFT:
					VisualModelTransformer.translateSelection(e.getModel(), -1,0);
					break;
				case KeyEvent.VK_RIGHT:
					VisualModelTransformer.translateSelection(e.getModel(), 1,0);
					break;
				case KeyEvent.VK_UP:
					VisualModelTransformer.translateSelection(e.getModel(),0,-1);
					break;
				case KeyEvent.VK_DOWN:
					VisualModelTransformer.translateSelection(e.getModel(),0,1);
					break;
				}
			} else { // Shift is pressed

				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_RIGHT:
					VisualModelTransformer.scaleSelection(e.getModel(),-1,1);
					break;
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
					VisualModelTransformer.scaleSelection(e.getModel(),1,-1);
					break;
				}
			}
		}

		if (e.isCtrlDown()) {
			switch(e.getKeyCode()){
			case KeyEvent.VK_G:
				e.getModel().groupSelection();
				e.getEditor().repaint();
				break;
			case KeyEvent.VK_U:
				e.getModel().ungroupSelection();
				e.getEditor().repaint();
				break;
			case KeyEvent.VK_C:
				break;
			case KeyEvent.VK_X:
				break;
			case KeyEvent.VK_V:
				e.getModel().selectNone();
				//addToSelection(e.getModel(), e.getModel().paste(Toolkit.getDefaultToolkit().getSystemClipboard(), prevPosition));
				//e.getModel().fireSelectionChanged();
				e.getEditor().repaint();
			}
		}
	}

	protected void currentLevelDown(VisualModel model) {
		Collection<Node> selection = model.getSelection();
		if(selection.size() == 1)
		{
			Node selectedNode = selection.iterator().next();
			if(selectedNode instanceof Container)
				model.setCurrentLevel((Container)selectedNode);
		}
		grayOutNotActive(model);
	}

	protected void currentLevelUp(VisualModel model) {
		Container level = model.getCurrentLevel();
		Container parent = Hierarchy.getNearestAncestor(level.getParent(), Container.class);
		if(parent!=null)
		{
			model.setCurrentLevel(parent);
			model.addToSelection(level);
			grayOutNotActive(model);
		}
	}

	private void offsetSelection(GraphEditorMouseEvent e, double dx, double dy) {
		VisualModel model = e.getEditor().getModel();
		//System.out.println (model.getSelection().size());

		for(Node node : model.getSelection()){
			if(node instanceof Movable) {
				Movable mv = (Movable) node;
				MovableHelper.translate(mv, dx, dy);
			}
		}
	}

	private Rectangle2D selectionRect(Point2D startPosition, Point2D currentPosition) {
		return new Rectangle2D.Double(
				Math.min(startPosition.getX(), currentPosition.getX()),
				Math.min(startPosition.getY(), currentPosition.getY()),
				Math.abs(startPosition.getX()-currentPosition.getX()),
				Math.abs(startPosition.getY()-currentPosition.getY())
		);
	}

	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		if(drag==DRAG_SELECT && selectionBox!=null) {
			g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));

			g.setColor(selectionFillColor);
			g.fill(selectionBox);
			g.setColor(selectionBorderColor);
			g.draw(selectionBox);
		}
	}

	public String getLabel() {
		return "Select";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_S;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/select.svg");
	}

}
