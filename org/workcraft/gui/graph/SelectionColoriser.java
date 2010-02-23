package org.workcraft.gui.graph;

import java.awt.Color;
import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Colorisable;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;

public class SelectionColoriser implements StateObserver {
	protected static Color selectionColor = new Color(99, 130, 191).brighter();

	private VisualModel model = null;

	public SelectionColoriser(VisualModel model) {
		this.model = model;
	}

	public void activate() {
		model.addObserver(this);
		update();
	}

	public void deactivate() {
		model.removeObserver(this);
		clear();
	}

	public VisualModel getModel() {
		return model;
	}

	protected void clear() {
		if(model==null)
			return;
		((VisualGroup) model.getRoot()).clearColorisation();
	}

	public void update() {
		if(model==null)
			return;
		((VisualGroup) model.getRoot()).clearColorisation();
		colorise(model.getSelection());
	}

	@Override
	public void notify(StateEvent e) {
		if(e instanceof SelectionChangedEvent) {
			SelectionChangedEvent se = (SelectionChangedEvent) e;
			uncolorise(se.getPrevSelection());
			colorise(se.getSelection());
		}
	}

	public static void colorise(Collection<Node> nodes) {
		for (Node n : nodes)
			if (n instanceof Colorisable)
				((Colorisable)n).setColorisation(selectionColor);
	}

	public static void colorise(Node node) {
		if (node instanceof Colorisable)
			((Colorisable)node).setColorisation(selectionColor);
	}

	public static void uncolorise(Collection<Node> nodes) {
		for (Node n : nodes)
			if (n instanceof Colorisable)
				((Colorisable)n).clearColorisation();
	}

	public static void uncolorise(Node node) {
		if (node instanceof Colorisable)
			((Colorisable)node).clearColorisation();
	}
}
