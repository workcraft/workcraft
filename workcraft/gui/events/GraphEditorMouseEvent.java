package org.workcraft.gui.events;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;

public class GraphEditorMouseEvent {

	private VisualModel model;
	private int event;
	private Point2D position;
	private int button;
	private int clickCount;
	private int modifiers;

	public GraphEditorMouseEvent(VisualModel model, MouseEvent e) {
		this.model = model;
		event = e.getID();
		if(model!=null && model.getEditorPane()!=null)
			position = model.getEditorPane().getViewport().screenToUser(e.getPoint());
		else
			position = new Point2D.Double(0, 0);
		button = e.getButton();
		clickCount = e.getClickCount();
		modifiers = e.getModifiersEx();
	}

	public GraphEditorMouseEvent(VisualModel model, int event, Point2D position, int button, int clickCount, int modifiers) {
		this.model = model;
		this.event = event;
		this.position = position;
		this.button = button;
		this.clickCount = clickCount;
		this.modifiers = modifiers;
	}

	public VisualModel getModel() {
		return model;
	}

	public int getEvent() {
		return event;
	}

	public Point2D getPosition() {
		return position;
	}

	public double getX() {
		return position.getX();
	}

	public double getY() {
		return position.getY();
	}

	public int getButton() {
		return button;
	}

	public int getClickCount() {
		return clickCount;
	}

	public int getModifiers() {
		return modifiers;
	}

}
