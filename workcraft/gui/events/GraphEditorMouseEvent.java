package org.workcraft.gui.events;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.workcraft.gui.edit.graph.GraphEditorPane;

public class GraphEditorMouseEvent {

	private GraphEditorPane editor;
	private int event;
	private Point2D position;
	private int button;
	private int clickCount;
	private int modifiers;

	public GraphEditorMouseEvent(GraphEditorPane editor, MouseEvent e) {
		this.editor = editor;
		this.event = e.getID();
		if(editor!=null)
			this.position = editor.getViewport().screenToUser(e.getPoint());
		else
			this.position = new Point2D.Double(0, 0);
		this.button = e.getButton();
		this.clickCount = e.getClickCount();
		this.modifiers = e.getModifiersEx();
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
		return this.editor;
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
