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

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1) {
			if(drag!=DRAG_NONE)
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

	@Override
	public void mouseEntered(GraphEditorMouseEvent e) {
	}

	@Override
	public void mouseExited(GraphEditorMouseEvent e) {
		// TODO very important! cancel selection upon changing document
		if(drag!=DRAG_NONE)
			cancelDrag(e); // TODO pan is better
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		if(drag==DRAG_MOVE) {
			Point2D pos = new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY());
			e.getModel().getEditorPane().snap(pos);
			offsetSelection(e, pos.getX()-prevPosition.getX(), pos.getY()-prevPosition.getY());
			e.getModel().getEditorPane().repaint();
			prevPosition = pos;
		}
		else if(drag==DRAG_SELECT) {
			LinkedList<Selectable> hit = e.getModel().getRoot().hitObjects(selectionRect(e.getPosition()));
			e.getModel().selectNone();
			e.getModel().selection().addAll(savedSelection);
			for(Selectable so : hit) {
				if(selectionMode==SELECTION_ADD) {
					e.getModel().addToSelection(so);
				}
				else if(selectionMode==SELECTION_REMOVE) {
					e.getModel().removeFromSelection(so);
				}
			}
			e.getModel().getEditorPane().repaint();
			prevPosition = e.getPosition();
		}
		else
			prevPosition = e.getPosition();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1) {
			startPosition = e.getPosition();
			prevPosition = e.getPosition();
			Selectable so = e.getModel().getRoot().hitObject(e.getPosition());
			if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so!=null) {
				if(!e.getModel().isObjectSelected(so)) {
					e.getModel().selectNone();
					e.getModel().addToSelection(so);
				}
				drag = DRAG_MOVE;
				if(so instanceof VisualNode) {
					VisualNode node = (VisualNode) so;
					snapOffset = new Point2D.Double(node.getX()-e.getX(), node.getY()-e.getY());
					prevPosition = new Point2D.Double(node.getX(), node.getY());
				}
				else
					snapOffset = new Point2D.Double(0, 0);
			}
			else {
				savedSelection.clear();
				if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so==null) {
					e.getModel().selectNone();
				}
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					selectionMode = SELECTION_REMOVE;
				else
					selectionMode = SELECTION_ADD;
				savedSelection.addAll(e.getModel().selection());
				drag = DRAG_SELECT;
			}
			e.getModel().getEditorPane().repaint();
		}
		else if(e.getButton()==MouseEvent.BUTTON3) {
			if(drag!=DRAG_NONE)
				cancelDrag(e);
		}
	}

	@Override
	public void mouseReleased(GraphEditorMouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1) {
			drag = DRAG_NONE;
			e.getModel().getEditorPane().repaint();
		}
	}

	private void offsetSelection(GraphEditorMouseEvent e, double dx, double dy) {
		for(Selectable so : e.getModel().selection()) {
			if(so instanceof VisualNode) {
				VisualNode node = (VisualNode) so;
				node.setX(node.getX()+dx);
				node.setY(node.getY()+dy);
			}
		}
	}

	private void cancelDrag(GraphEditorMouseEvent e) {
		if(drag==DRAG_MOVE) {
			offsetSelection(e, startPosition.getX()-e.getX(), startPosition.getY()-e.getY());
		}
		else if(drag == DRAG_SELECT) {
			e.getModel().selectNone();
			e.getModel().selection().addAll(savedSelection);
			savedSelection.clear();
		}
		drag = DRAG_NONE;
		e.getModel().getEditorPane().repaint();
	}

	private Rectangle2D selectionRect(Point2D currentPosition) {
		return new Rectangle2D.Double(
				Math.min(startPosition.getX(), currentPosition.getX()),
				Math.min(startPosition.getY(), currentPosition.getY()),
				Math.abs(startPosition.getX()-currentPosition.getX()),
				Math.abs(startPosition.getY()-currentPosition.getY())
			);
	}

	@Override
	public void draw(GraphEditorPane editor, Graphics2D g) {
		if(drag==DRAG_SELECT) {
			g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));
			Rectangle2D bb = selectionRect(prevPosition);
			g.setColor(new Color(128, 128, 128, 32));
			g.fill(bb);
			g.setColor(new Color(128, 128, 128));
			g.draw(bb);
		}
	}

}
