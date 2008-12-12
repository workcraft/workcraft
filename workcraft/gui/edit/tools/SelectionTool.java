package org.workcraft.gui.edit.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.LinkedList;

import org.workcraft.dom.visual.Selectable;
import org.workcraft.dom.visual.VisualConnection;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.edit.graph.GraphEditorPane;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class SelectionTool implements GraphEditorTool {
	private static final int DRAG_NONE = 0;
	private static final int DRAG_MOVE = 1;
	private static final int DRAG_SELECT = 2;

	private static final int SELECTION_ADD = 0;
	private static final int SELECTION_REMOVE = 1;

	protected Color selectionBorderColor = new Color(200, 200, 200);
	//	protected Color selectionFillColor = new Color(200, 200, 200, 32);
	protected Color selectionFillColor = new Color(99, 130, 191, 32);

	private int drag = DRAG_NONE;
	private Point2D prevPosition;
	private Point2D startPosition;
	private Point2D snapOffset;
	private LinkedList<Selectable> savedSelection = new LinkedList<Selectable>();
	private int selectionMode;


	public void mouseClicked(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			if(drag!=DRAG_NONE)
				cancelDrag(e);
			if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0)
				model.selection().clear();
			Selectable so = model.getRoot().hitObject(e.getPosition());
			if(so!=null)
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					model.removeFromSelection(so);
				else
					model.addToSelection(so);
			model.fireLayoutChanged();
		}
		else if(e.getButton()==MouseEvent.BUTTON3) {
			// TODO show tool popup
		}
	}


	public void mouseEntered(GraphEditorMouseEvent e) {
	}


	public void mouseExited(GraphEditorMouseEvent e) {
		//		if(this.drag!=DRAG_NONE)
		//		cancelDrag(e); // TODO pan is better
	}


	public void mouseMoved(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE) {
			Point2D pos = new Point2D.Double(e.getX()+snapOffset.getX(), e.getY()+snapOffset.getY());
			e.getEditor().snap(pos);
			offsetSelection(e, pos.getX()-prevPosition.getX(), pos.getY()-prevPosition.getY());
			prevPosition = pos;
		}
		else if(drag==DRAG_SELECT) {
			LinkedList<Selectable> hit = model.getRoot().hitObjects(selectionRect(e.getPosition()));
			model.selectNone();
			model.selection().addAll(savedSelection);
			for(Selectable so : hit)
				if(selectionMode==SELECTION_ADD)
					model.addToSelection(so);
				else if(selectionMode==SELECTION_REMOVE)
					model.removeFromSelection(so);
			model.fireLayoutChanged();
			prevPosition = e.getPosition();
		}
		else
			prevPosition = e.getPosition();
	}


	public void mousePressed(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			startPosition = e.getPosition();
			prevPosition = e.getPosition();
			Selectable so = model.getRoot().hitObject(e.getPosition());
			if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so!=null) {
				if(!model.isObjectSelected(so)) {
					model.selectNone();
					model.addToSelection(so);
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
				if((e.getModifiers()&(MouseEvent.SHIFT_DOWN_MASK|MouseEvent.ALT_DOWN_MASK))==0 && so==null)
					model.selectNone();
				if((e.getModifiers()&MouseEvent.ALT_DOWN_MASK)!=0)
					selectionMode = SELECTION_REMOVE;
				else
					selectionMode = SELECTION_ADD;
				savedSelection.addAll(model.selection());
				drag = DRAG_SELECT;
			}
			model.fireLayoutChanged();
		}
		else if(e.getButton()==MouseEvent.BUTTON3)
			if(drag!=DRAG_NONE)
				cancelDrag(e);
	}


	public void mouseReleased(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(e.getButton()==MouseEvent.BUTTON1) {
			drag = DRAG_NONE;
			model.fireLayoutChanged();
		}
	}

	private void offsetSelection(GraphEditorMouseEvent e, double dx, double dy) {
		VisualModel model = e.getEditor().getModel().getVisualModel();

		for(Selectable so : model.selection())
			if(so instanceof VisualNode) {
				VisualNode node = (VisualNode) so;
				node.setX(node.getX()+dx);
				node.setY(node.getY()+dy);
			}

		model.fireLayoutChanged();
	}

	private void cancelDrag(GraphEditorMouseEvent e) {
		VisualModel model = e.getEditor().getModel();

		if(drag==DRAG_MOVE)
			offsetSelection(e, startPosition.getX()-e.getX(), startPosition.getY()-e.getY());
		else if(drag == DRAG_SELECT) {
			model.selectNone();
			model.selection().addAll(savedSelection);
			savedSelection.clear();
		}
		drag = DRAG_NONE;
		model.fireLayoutChanged();
	}

	private Rectangle2D selectionRect(Point2D currentPosition) {
		return new Rectangle2D.Double(
				Math.min(startPosition.getX(), currentPosition.getX()),
				Math.min(startPosition.getY(), currentPosition.getY()),
				Math.abs(startPosition.getX()-currentPosition.getX()),
				Math.abs(startPosition.getY()-currentPosition.getY())
		);
	}


	public void drawInUserSpace(GraphEditorPane editor, Graphics2D g) {
		VisualModel model = editor.getModel();
		g.setStroke(new BasicStroke((float) editor.getViewport().pixelSizeInUserSpace().getX()));

		Rectangle2D.Double rect = null;
		for(Selectable vo : model.getSelection()) {

			if(vo==null)
				continue;


			Rectangle2D bb = vo.getBoundingBoxInUserSpace();

			if (bb == null) {
				System.err.println (vo.getClass().getName());
				return;
			}

			if(rect==null) {
				rect = new Rectangle2D.Double();
				rect.setRect(bb);
			}
			else
				rect.add(bb);
			if(vo instanceof VisualConnection)
				continue; // TODO somehow show selected connections
				g.setColor(new Color(255, 0, 0, 64));
				g.fill(bb);
				g.setColor(new Color(255, 0, 0));
				g.draw(bb);
		}
		if(rect!=null) {
			g.setColor(new Color(255, 0, 0, 128));
			g.draw(rect);
		}

		if(drag==DRAG_SELECT) {
			Rectangle2D bb = selectionRect(prevPosition);
			g.setColor(selectionFillColor);
			g.fill(bb);
			g.setColor(selectionBorderColor);
			g.draw(bb);

		}
	}

	public void drawInScreenSpace(GraphEditorPane editor, Graphics2D g) {

	}

	public String getIconPath() {
		return "org" + File.separator + "workcraft" + File.separator + "gui" + File.separator + "icons" + File.separator + "select.png";
	}

	public String getName() {
		return "Selection Tool";
	}

}
