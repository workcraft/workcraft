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
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Colorisable;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;

public class SelectionTool extends AbstractTool implements StateObserver {
	private static final int DRAG_NONE = 0;
	private static final int DRAG_MOVE = 1;
	private static final int DRAG_SELECT = 2;

	private static final int SELECTION_NONE = 0;
	private static final int SELECTION_ADD = 1;
	private static final int SELECTION_REMOVE = 2;
	private static final int SELECTION_REPLACE = 3;


	protected Color selectionBorderColor = new Color(200, 200, 200);
	protected Color selectionFillColor = new Color(99, 130, 191, 32);
	protected Color selectionColor = new Color(99, 130, 191).brighter();;
	protected Color grayOutColor = Color.LIGHT_GRAY;

	private int drag = DRAG_NONE;
	private boolean notClick = false;

	private Point2D snapOffset;

	private LinkedList<Node> selected = new LinkedList<Node>();
	private int selectionMode;

	private Rectangle2D selectionBox = null;

	@Override
	public void activated(GraphEditor editor) {
		editor.getModel().addObserver(this);
	}

	@Override
	public void deactivated(GraphEditor editor) {
		editor.getModel().removeObserver(this);
	}

	private void selectNone(VisualModel model) {
		uncolorise (model.getSelection());
		model.selectNone();
	}

	private void colorise(Collection<Node> nodes) {
		for (Node n : nodes)
			if (n instanceof Colorisable)
				((Colorisable)n).setColorisation(selectionColor);
	}

	private void colorise(Node node) {
		if (node instanceof Colorisable)
			((Colorisable)node).setColorisation(selectionColor);
	}

	private void uncolorise(Collection<Node> nodes) {
		for (Node n : nodes)
			if (n instanceof Colorisable)
				((Colorisable)n).clearColorisation();
	}

	private void uncolorise(Node node) {
		if (node instanceof Colorisable)
			((Colorisable)node).clearColorisation();
	}

	private void select (VisualModel model, VisualNode node) {
		uncolorise(model.getSelection());
		colorise(node);
		model.select(node);
	}

	private void select (VisualModel model, Collection<Node> nodes) {
		uncolorise(model.getSelection());
		colorise (nodes);
		model.select(nodes);
	}

	protected void addToSelection(VisualModel model, Node so) {
		colorise(so);
		model.addToSelection(so);
	}

	protected void addToSelection(VisualModel model, Collection<Node> s) {
		colorise(s);
		model.addToSelection(s);
	}

	protected void removeFromSelection(VisualModel model, VisualNode so) {
		uncolorise(so);
		model.removeFromSelection(so);
	}

	protected void removeFromSelection(VisualModel model, Collection<Node> so) {
		uncolorise(so);
		model.removeFromSelection(so);
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
						select(e.getModel(), node);
						break;
					case MouseEvent.SHIFT_DOWN_MASK:
						addToSelection(e.getModel(), node);
						break;
					case MouseEvent.CTRL_DOWN_MASK:
						removeFromSelection(e.getModel(), node);
						break;
				}
			} else {
				if (e.getKeyModifiers()==0)
					selectNone(e.getModel());
			}
		}
		else if(e.getButton()==MouseEvent.BUTTON3 && !notClick) {

			/* POPUP MENU */
			// FIXME implement real popup menu
			JPopupMenu popup = new JPopupMenu();
			popup.add(new JMenuItem("Test popup menu"));
			popup.show(e.getSystemEvent().getComponent(), e.getSystemEvent().getX(), e.getSystemEvent().getY());

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
			uncolorise(selected);
			selected.clear();
			selected.addAll(model.boxHitTest(e.getStartPosition(), e.getPosition()));

			colorise(e.getModel().getSelection());
			if (selectionMode == SELECTION_ADD || selectionMode == SELECTION_REPLACE) {
				colorise(selected);
			} else {
				uncolorise(selected);
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
						selectNone(model);
					else
						selected.addAll(model.getSelection());
				}

			} else {
				// hit something

				if(e.getKeyModifiers()==0 && hitNode instanceof Movable) {
					// mouse down without modifiers, begin move-drag
					drag = DRAG_MOVE;

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
		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {

			VisualNode hitNode = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			if(hitNode!=null && !model.getSelection().contains(hitNode))
				select(e.getModel(), hitNode);

		}
		else if(e.getButton()==MouseEvent.BUTTON3) {

			if(isDragging()) {
				cancelDrag(e);
				e.getEditor().repaint();
				notClick = true; // FIXME left click still generated but it should not
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
				select(e.getModel(), selected);
			else if (selectionMode == SELECTION_ADD)
				addToSelection(e.getModel(), selected);
			else if (selectionMode == SELECTION_REMOVE)
				removeFromSelection(e.getModel(), selected);
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
//			offsetSelection(e, e.getStartPosition().getX()-e.getX(), e.getStartPosition().getY()-e.getY());
		}
		else if(drag == DRAG_SELECT) {
			uncolorise(selected);
			colorise(model.getSelection());
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

		model.getCurrentLevel().clearColorisation();
		for(Node node : model.getSelection())
			if(node instanceof Colorisable)
				((Colorisable)node).setColorisation(selectionColor);
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
					currentLevelUp(e);
					break;
				case KeyEvent.VK_PAGE_DOWN:
					currentLevelDown(e);
					break;
				case KeyEvent.VK_OPEN_BRACKET:
					//e.getModel().rotateSelection(-Math.PI/2);
					break;
				case KeyEvent.VK_CLOSE_BRACKET:
					//e.getModel().rotateSelection(Math.PI/2);
					break;
				case KeyEvent.VK_LEFT:
					//e.getModel().translateSelection(-1,0);
					break;
				case KeyEvent.VK_RIGHT:
					//e.getModel().translateSelection(1,0);
					break;
				case KeyEvent.VK_UP:
					//e.getModel().translateSelection(0,-1);
					break;
				case KeyEvent.VK_DOWN:
					//	e.getModel().translateSelection(0,1);
					break;
				}
			} else { // Shift is pressed

				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					//e.getModel().scaleSelection(-1,1);
					break;
				case KeyEvent.VK_UP:
					//e.getModel().scaleSelection(1,-1);
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
				selectNone(e.getModel());
				//addToSelection(e.getModel(), e.getModel().paste(Toolkit.getDefaultToolkit().getSystemClipboard(), prevPosition));
				//e.getModel().fireSelectionChanged();
				e.getEditor().repaint();
			}
		}
	}

	private void currentLevelDown(GraphEditorKeyEvent e) {
		Collection<Node> selection = e.getModel().getSelection();
		if(selection.size() == 1)
		{
			Node selectedNode = selection.iterator().next();
			if(selectedNode instanceof VisualGroup)
				e.getModel().setCurrentLevel((VisualGroup)selectedNode);
		}
		grayOutNotActive(e.getModel());
	}

	private void currentLevelUp(GraphEditorKeyEvent e) {
		VisualGroup level = e.getModel().getCurrentLevel();
		VisualGroup parent = Hierarchy.getNearestAncestor(level.getParent(), VisualGroup.class);
		if(parent!=null)
		{
			e.getModel().setCurrentLevel(parent);
			e.getModel().addToSelection(level);
			grayOutNotActive(e.getModel());
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
		try {
			return GUI.loadIconFromResource("images/select.png");
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void notify(StateEvent e) {
		// TODO Auto-generated method stub

	}
}