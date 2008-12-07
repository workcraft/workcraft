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
		this.event = e.getID();
		if(model!=null && model.getEditorPane()!=null)
			this.position = model.getEditorPane().getViewport().screenToUser(e.getPoint());
		else
			this.position = new Point2D.Double(0, 0);
		this.button = e.getButton();
		this.clickCount = e.getClickCount();
		this.modifiers = e.getModifiersEx();
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
		return this.model;
	}

	public int getEvent() {
		return this.event;
	}

	public Point2D getPosition() {
		return this.position;
	}

	public double getX() {
		return this.position.getX();
	}

	public double getY() {
		return this.position.getY();
	}

	public int getButton() {
		return this.button;
	}

	public int getClickCount() {
		return this.clickCount;
	}

	public int getModifiers() {
		return this.modifiers;
	}

}
