package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.ContainerDecoration;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.util.Hierarchy;


@Hotkey(KeyEvent.VK_P)
@DisplayName("Page")
@SVGIcon("images/icons/svg/page.svg")
public class VisualPage extends VisualComponent implements Drawable, Collapsible, Container, ObservableHierarchy {

	protected final double margin = 0.20;

	private boolean isCollapsed = false;
	@Override
	public void setIsCollapsed(boolean isCollapsed) {
		sendNotification(new TransformChangingEvent(this));
		this.isCollapsed = isCollapsed;
		sendNotification(new TransformChangedEvent(this));
	}

	@Override
	public boolean getIsCollapsed() {
		return isCollapsed&&!isExcited;
	}

	private boolean isExcited = false;
	public void setIsExcited(boolean isExcited) {
		if (this.isExcited==isExcited) return;

		sendNotification(new TransformChangingEvent(this));
		this.isExcited = isExcited;
		sendNotification(new TransformChangedEvent(this));
	}


	private String referencedModel = "";
	public void setReferencedModel(String model) {
		sendNotification(new TransformChangingEvent(this));
		this.referencedModel = model;
		sendNotification(new TransformChangedEvent(this));
	}

	public String getReferencedModel() {
		return referencedModel;
	}

	public Collection<VisualComponent> getComponents(){

		return Hierarchy.getDescendantsOfType(this, VisualComponent.class);

	}

	public Collection<VisualConnection> getConnections(){

		return Hierarchy.getDescendantsOfType(this, VisualConnection.class);

	}


	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualPage, Boolean>(
				this, "Is collapsed", Boolean.class) {

			@Override
			protected void setter(VisualPage object, Boolean value) {
				object.setIsCollapsed(value);
			}
			@Override
			protected Boolean getter(VisualPage object) {
				return object.getIsCollapsed();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualPage, String>(
				this, "Referenced model", String.class) {

			@Override
			protected void setter(VisualPage object, String value) {
				object.setReferencedModel(value);
			}
			@Override
			protected String getter(VisualPage object) {
				return object.getReferencedModel();
			}
		});
	}


	private boolean isInside = false;
	@Override
	public void setIsCurrentLevelInside(boolean isInside) {
		sendNotification(new TransformChangingEvent(this));
		this.isInside = isInside;
		sendNotification(new TransformChangedEvent(this));
	}

	public boolean isCurrentLevelInside() {
		return isInside;
	}

	private DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

	public VisualPage(MathNode refNode) {
		super(refNode);

		addPropertyDeclarations();
	}

	public List<Node> unGroup(ReferenceManager mathManager) {
		ArrayList<Node> nodesToReparent = new ArrayList<Node>(groupImpl.getChildren());

		Container newParent = Hierarchy.getNearestAncestor(getParent(), Container.class);

		groupImpl.reparent(nodesToReparent, newParent);

		for (Node node : nodesToReparent)
			TransformHelper.applyTransform(node, localToParentTransform);

		PageNode page = (PageNode)getReferencedComponent();

		page.unGroup(mathManager);

		return nodesToReparent;
	}


	@Override
	public void add(Node node) {
		groupImpl.add(node);
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	@Override
	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	@Override
	public Node getParent() {
		return groupImpl.getParent();
	}

	@Override
	public void remove(Node node) {
		groupImpl.remove(node);
	}


	@Override
	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	@Override
	public void removeAllObservers() {
		groupImpl.removeAllObservers();
	}

	@Override
	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}


	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}


	@Override
	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
	}


	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}


	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public Rectangle2D getInternalBoundingBoxInLocalSpace() {
		if (groupImpl == null) {
			return super.getInternalBoundingBoxInLocalSpace();
		}
		if (getIsCollapsed() && !isCurrentLevelInside()) {
	        return super.getInternalBoundingBoxInLocalSpace();
		} else {
			Collection<Touchable> children = Hierarchy.getChildrenOfType(this, Touchable.class);
			Rectangle2D bb = BoundingBoxHelper.mergeBoundingBoxes(children);
			if (bb == null) {
				bb = super.getInternalBoundingBoxInLocalSpace();
			}
			return BoundingBoxHelper.expand(bb, margin, margin);
		}
	}

	@Override
	public void draw(DrawRequest r) {
		Decoration dec = r.getDecoration();
		if (dec instanceof ContainerDecoration) {
			setIsExcited(((ContainerDecoration)dec).isContainerExcited());
		}
		// This is to update the rendered text for names (and labels) of group children,
		// which is necessary to calculate the bounding box before children have been drawn
		for (VisualComponent component: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
			component.cacheRenderedText(r);
		}

		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		if ((bb != null) && (getParent() != null)) {
			Graphics2D g = r.getGraphics();
			bb.setRect(bb.getX(), bb.getY(), bb.getWidth(), bb.getHeight());
			if (getIsCollapsed() && !isCurrentLevelInside()) {
				g.setColor(Coloriser.colorise(this.getFillColor(), r.getDecoration().getColorisation()));
				g.fill(bb);
			}
			float[] pattern = {0.2f, 0.2f};
			g.setStroke(new BasicStroke(0.05f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
			g.setColor(Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation()));
			g.draw(bb);
			drawNameInLocalSpace(r);
			drawLabelInLocalSpace(r);
		}
	}
}
