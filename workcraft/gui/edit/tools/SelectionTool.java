package org.workcraft.gui.edit.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.workcraft.dom.visual.Selectable;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.edit.graph.GraphEditorPane;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class SelectionTool implements GraphEditorTool {
	private static final int DRAG_NONE = 0;
	private static final int DRAG_MOVE = 1;
	private static final int DRAG_SELECT = 2;

	private static final int SELECTION_ADD = 0;
	private static final int SELECTION_REMOVE = 1;

	private int drag = DRAG_NONE;
	private Point2D prevPosition;
	private Point2D startPosition;
	private Point2D snapOffset;
	private LinkedList<Selectable> savedSelection = new LinkedList<Selectable>();
	private int selectionMode;


	public void mouseClicked(GraphEditorMouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1) {
			if(this.drag!=DRAG_NONE)
				cancelDrag(e);
			if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0)
				e.getModel().selection().clear();
			Selectable so = e.getModel().getRoot().hitObject(e.getPosition());
			if(so!=null)
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					e.getModel().removeFromSelection(so);
				else
					e.getModel().addToSelection(so);
			e.getModel().getEditorPane().repaint();
		}
		else if(e.getButton()==MouseEvent.BUTTON3) {
			// TODO show tool popup
		}
	}


	public void mouseEntered(GraphEditorMouseEvent e) {
	}


	public void mouseExited(GraphEditorMouseEvent e) {
		// TODO very important! cancel selection upon changing document
		if(this.drag!=DRAG_NONE)
			cancelDrag(e); // TODO pan is better
	}


	public void mouseMoved(GraphEditorMouseEvent e) {
		if(this.drag==DRAG_MOVE) {
			Point2D pos = new Point2D.Double(e.getX()+this.snapOffset.getX(), e.getY()+this.snapOffset.getY());
			e.getModel().getEditorPane().snap(pos);
			offsetSelection(e, pos.getX()-this.prevPosition.getX(), pos.getY()-this.prevPosition.getY());
			e.getModel().getEditorPane().repaint();
			this.prevPosition = pos;
		}
		else if(this.drag==DRAG_SELECT) {
			LinkedList<Selectable> hit = e.getModel().getRoot().hitObjects(selectionRect(e.getPosition()));
			e.getModel().selectNone();
			e.getModel().selection().addAll(this.savedSelection);
			for(Selectable so : hit)
				if(this.selectionMode==SELECTION_ADD)
					e.getModel().addToSelection(so);
				else if(this.selectionMode==SELECTION_REMOVE)
					e.getModel().removeFromSelection(so);
			e.getModel().getEditorPane().repaint();
			this.prevPosition = e.getPosition();
		}
		else
			this.prevPosition = e.getPosition();
	}


	public void mousePressed(GraphEditorMouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1) {
			this.startPosition = e.getPosition();
			this.prevPosition = e.getPosition();
			Selectable so = e.getModel().getRoot().hitObject(e.getPosition());
			if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so!=null) {
				if(!e.getModel().isObjectSelected(so)) {
					e.getModel().selectNone();
					e.getModel().addToSelection(so);
				}
				this.drag = DRAG_MOVE;
				if(so instanceof VisualNode) {
					VisualNode node = (VisualNode) so;
					this.snapOffset = new Point2D.Double(node.getX()-e.getX(), node.getY()-e.getY());
					this.prevPosition = new Point2D.Double(node.getX(), node.getY());
				}
				else
					this.snapOffset = new Point2D.Double(0, 0);
			}
			else {
				this.savedSelection.clear();
				if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so==null)
					e.getModel().selectNone();
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					this.selectionMode = SELECTION_REMOVE;
				else
					this.selectionMode = SELECTION_ADD;
				this.savedSelection.addAll(e.getModel().selection());
				this.drag = DRAG_SELECT;
			}
			e.getModel().getEditorPane().repaint();
		}
		else if(e.getButton()==MouseEvent.BUTTON3)
			if(this.drag!=DRAG_NONE)
				cancelDrag(e);
	}


	public void mouseReleased(GraphEditorMouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1) {
			this.drag = DRAG_NONE;
			e.getModel().getEditorPane().repaint();
		}
	}

	private void offsetSelection(GraphEditorMouseEvent e, double dx, double dy) {
		for(Selectable so : e.getModel().selection())
			if(so instanceof VisualNode) {
				VisualNode node = (VisualNode) so;
				node.setX(node.getX()+dx);
				node.setY(node.getY()+dy);
			}
	}

	private void cancelDrag(GraphEditorMouseEvent e) {
		if(this.drag==DRAG_MOVE)
			offsetSelection(e, this.startPosition.getX()-e.getX(), this.startPosition.getY()-e.getY());
		else if(this.drag == DRAG_SELECT) {
			e.getModel().selectNone();
			e.getModel().selection().addAll(this.savedSelection);
			this.savedSelection.clear();
		}
		this.drag = DRAG_NONE;
		e.getModel().getEditorPane().repaint();
	}

	private Rectangle2D selectionRect(Point2D currentPosition) {
		return new Rectangle2D.Double(
				Math.min(this.startPosition.getX(), currentPosition.getX()),
				Math.min(this.startPosition.getY(), currentPosition.getY()),
				Math.abs(this.startPosition.getX()-currentPosition.getX()),
				Math.abs(this.startPosition.getY()-currentPosition.getY())
		);
	}


	public void draw(GraphEditorPane editor, Graphics2D g) {
		if(this.drag==DRAG_SELECT) {
			g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));
			Rectangle2D bb = selectionRect(this.prevPosition);
			g.setColor(new Color(128, 128, 128, 32));
			g.fill(bb);
			g.setColor(new Color(128, 128, 128));
			g.draw(bb);
		}
	}

}
