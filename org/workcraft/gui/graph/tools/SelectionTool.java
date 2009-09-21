package org.workcraft.gui.graph.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.Icon;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Colorisable;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.MovableHelper;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.util.Hierarchy;

public class SelectionTool extends AbstractTool {
	private static final int DRAG_NONE = 0;
	private static final int DRAG_MOVE = 1;
	private static final int DRAG_SELECT = 2;

	private static final int SELECTION_ADD = 0;
	private static final int SELECTION_REMOVE = 1;
	private static final int SELECTION_REPLACE = 2;


	protected Color selectionBorderColor = new Color(200, 200, 200);
	protected Color selectionFillColor = new Color(99, 130, 191, 32);
	protected Color selectionColor = new Color(99, 130, 191).brighter();;
	protected Color grayOutColor = Color.LIGHT_GRAY;

	private int drag = DRAG_NONE;
	private Point2D prevPosition;
	private Point2D startPosition;
	private Point2D snapOffset;
	private LinkedList<Node> selected = new LinkedList<Node>();
	private int selectionMode;

	private static ListenerResolver listenerResolver = new ListenerResolver();
	private static Collection<GraphEditorMouseListener> listeners = Collections.emptyList();

	@Override
	public void activated(GraphEditor editor) {
		listeners = listenerResolver.getMouseListenersFor(editor.getModel().getClass());
	}

	@Override
	public void deactivated(GraphEditor editor) {
		listeners = Collections.emptyList();
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
	public void mouseClicked(GraphEditorMouseEvent e) {
		//System.out.println ("mouseClicking <_<");

		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			if(drag!=DRAG_NONE)
				stopDrag(e);

			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);

			if (node != null)
			{
				if ((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.CTRL_DOWN_MASK))==0) {
					// if node is selected and it is the only node in selection, do nothing
					if (!(e.getModel().getSelection().size() == 1 & e.getModel().getSelection().contains(node)))
						// else select only this node
						select(e.getModel(), node);
				} else
					if ( (e.getModifiers()&MouseEvent.SHIFT_DOWN_MASK) != 0)
						addToSelection(e.getModel(), node);
					else if ( (e.getModifiers()&MouseEvent.CTRL_DOWN_MASK) != 0)
						removeFromSelection(e.getModel(), node);
			} else {
				if ((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.CTRL_DOWN_MASK))==0)
					selectNone (e.getModel());
			}
		}
		else if(e.getButton()==MouseEvent.BUTTON3) {


			/* POPUP MENU */

		}

		for (GraphEditorMouseListener listener : listeners)
			listener.mouseClicked(e);

		//System.out.println ("mouseClicked >_>");
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			Point2D pos = new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY());
			e.getEditor().snap(pos);

			offsetSelection(e, pos.getX()-prevPosition.getX(), pos.getY()-prevPosition.getY());

			prevPosition = pos;
		}
		else if(drag==DRAG_SELECT) {
			Collection<Node> hit = model.boxHitTest(startPosition, e.getPosition());

			uncolorise(selected);

			selected.clear();
			selected.addAll(hit);

			colorise(e.getModel().getSelection());

			if (selectionMode == SELECTION_ADD || selectionMode == SELECTION_REPLACE) {
				colorise(selected);
			} else {
				uncolorise(selected);
			}

			prevPosition = e.getPosition();
			e.getEditor().repaint();
		}
		else
			prevPosition = e.getPosition();
	}


	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		// System.out.println ("mousePressing ^_^");

		// TODO: drag should start only if mouse is indeed moved, otherwise it interferes
		// with correct onclick behaviour

		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			startPosition = e.getPosition();
			prevPosition = e.getPosition();

			VisualNode hitNode = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);

			if (hitNode == null) {
				// hit nothing, so start select-drag
				// selection will not actually be changed until drag completes
				drag = DRAG_SELECT;
				selected.clear();

				// System.out.println ("Drag-select");

				if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.CTRL_DOWN_MASK))==0) {
					// no modifiers, start new selection
					selectNone(model);
					selectionMode = SELECTION_REPLACE;
				} else {
					// remember what was selected when drag started to modify the selection accordingly
					selected.addAll(model.getSelection());

					if((e.getModifiers()&MouseEvent.CTRL_DOWN_MASK)!=0)
						// alt held
						selectionMode = SELECTION_REMOVE;
					else
						// shift held
						selectionMode = SELECTION_ADD;
				}
			} else {
				// hit something

				if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.CTRL_DOWN_MASK))==0) {
					// mouse down without modifiers, begin move-drag

					if(!model.getSelection().contains(hitNode))
						select(e.getModel(), hitNode);
					//System.out.println (e.getModel().getSelection().size() + " trying");
					//System.out.println (e.getModel().getSelection().iterator().next());

					drag = DRAG_MOVE;

					if(hitNode instanceof VisualTransformableNode) {
						VisualTransformableNode node = (VisualTransformableNode) hitNode;
						snapOffset = new Point2D.Double(node.getX()-e.getX(), node.getY()-e.getY());
						prevPosition = new Point2D.Double(node.getX(), node.getY());
					}

					else
						snapOffset = new Point2D.Double(0, 0);
				}
				// do nothing if pressed on a node with modifiers
			}
		}
		else if(e.getButton()==MouseEvent.BUTTON3)
			if(drag!=DRAG_NONE)
				stopDrag(e);

		//		System.out.println ("mousePressed d^_^b");
	}

	@Override
	public void mouseReleased(GraphEditorMouseEvent e) {
		//System.out.println ("mouseReleasing T_T");

		if(e.getButton()==MouseEvent.BUTTON1) {
			if (drag == DRAG_SELECT)
			{
				if (selectionMode == SELECTION_REPLACE)
					select(e.getModel(), selected);
				else if (selectionMode == SELECTION_ADD)
					addToSelection(e.getModel(), selected);
				else if (selectionMode == SELECTION_REMOVE)
					removeFromSelection(e.getModel(), selected);
			}
			drag = DRAG_NONE;

			e.getEditor().repaint();
		}
		//System.out.println ("mouseReleased X_X");
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

	private void stopDrag(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			offsetSelection(e, startPosition.getX()-e.getX(), startPosition.getY()-e.getY());
		}
		else if(drag == DRAG_SELECT) {
			selectNone(model);
			for (Node so: selected)
				addToSelection(model, so);
			selected.clear();
			// model.fireSelectionChanged();
		}
		drag = DRAG_NONE;

	}

	private Rectangle2D selectionRect(Point2D currentPosition) {
		return new Rectangle2D.Double(
				Math.min(startPosition.getX(), currentPosition.getX()),
				Math.min(startPosition.getY(), currentPosition.getY()),
				Math.abs(startPosition.getX()-currentPosition.getX()),
				Math.abs(startPosition.getY()-currentPosition.getY())
		);
	}

	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		if(drag==DRAG_SELECT) {
			g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));

			Rectangle2D bb = selectionRect(prevPosition);
			g.setColor(selectionFillColor);
			g.fill(bb);
			g.setColor(selectionBorderColor);
			g.draw(bb);
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
		return null;
	}
}