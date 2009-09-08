package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.framework.observation.ObservableState;

public interface VisualModel extends Model, ObservableState {
	public void draw (Graphics2D g);

	public VisualGroup getCurrentLevel();
	public Model getMathModel();

	public Collection<Node> getSelection();

	public void selectAll();
	public void selectNone();
	public void select(Node node);
	public void select(Collection<Node> node);
	public void addToSelection (Collection<Node> node);
	public void addToSelection (Node node);
	public void removeFromSelection (Node node);
	public void removeFromSelection (Collection<Node> nodes);
	public void deleteSelection();
	public void groupSelection();
	public void ungroupSelection();

	public void setCurrentLevel (VisualGroup group);

	public Collection<Node> boxHitTest(Point2D p1, Point2D p2);
	public Collection<Node> boxHitTest(Rectangle2D rect);
}