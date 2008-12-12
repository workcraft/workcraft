package org.workcraft.gui.events;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.edit.graph.GraphEditorPane;

public class GraphEditorMouseEvent {

	GraphEditorPane editor;
	VisualModel model;

	int event;
	Point2D position;
	int button;
	int clickCount;
	int modifiers;

	public GraphEditorMouseEvent(GraphEditorPane editor, MouseEvent e) {
		this.editor = editor;
		model = editor.getModel();

		event = e.getID();
		if(editor!=null)
			position = editor.getViewport().screenToUser(e.getPoint());
		else
			position = new Point2D.Double(0, 0);
		button = e.getButton();
		clickCount = e.getClickCount();
		modifiers = e.getModifiersEx();
	}

	public GraphEditorMouseEvent(GraphEditorPane editor, int event, Point2D position, int button, int clickCount, int modifiers) {
		this.editor = editor;
		this.event = event;
		this.position = position;
		this.button = button;
		this.clickCount = clickCount;
		this.modifiers = modifiers;
	}

	public GraphEditorPane getEditor() {
		return editor;
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
