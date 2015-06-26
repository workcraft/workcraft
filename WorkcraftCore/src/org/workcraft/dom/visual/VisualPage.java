package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
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
@SVGIcon("images/icons/svg/selection-page.svg")
public class VisualPage extends VisualComponent implements Drawable, Collapsible, Container, ObservableHierarchy {
	public static final String PROPERTY_IS_COLLAPSED = "Is collapsed";
	public static final String PROPERTY_REFERENCED_MODEL = "Referenced model";

	private boolean isCurrentLevelInside = false;
	private boolean isCollapsed = false;
	private boolean isExcited = false;
	private String referencedModel = "";
	private DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

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
				this, PROPERTY_IS_COLLAPSED, Boolean.class) {

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
				this, PROPERTY_REFERENCED_MODEL, String.class) {

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

	@Override
	public void setIsCurrentLevelInside(boolean value) {
		if (isCurrentLevelInside != value) {
			sendNotification(new TransformChangingEvent(this));
			isCurrentLevelInside = value;
			sendNotification(new TransformChangedEvent(this));
		}
	}

	@Override
	public boolean isCurrentLevelInside() {
		return isCurrentLevelInside;
	}

	@Override
	public void setIsCollapsed(boolean value) {
		if (isCollapsed != value) {
			sendNotification(new TransformChangingEvent(this));
			isCollapsed = value;
			sendNotification(new TransformChangedEvent(this));
		}
	}

	@Override
	public boolean getIsCollapsed() {
		return (isCollapsed && !isExcited);
	}

	@Override
	public void setIsExcited(boolean value) {
		if (isExcited != value) {
			sendNotification(new TransformChangingEvent(this));
			isExcited = value;
			sendNotification(new TransformChangedEvent(this));
		}
	}

	public VisualPage(MathNode refNode) {
		super(refNode);
		addPropertyDeclarations();
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

    public void removeWithoutNotify(Node node) {
        groupImpl.removeWithoutNotify(node);
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
			return BoundingBoxHelper.expand(bb, getExpansion().getX(), getExpansion().getY());
		}
	}

	public Point2D getExpansion() {
		return new Point2D.Double(0.2,  0.2);
	}

	@Override
	public void draw(DrawRequest r) {
		Decoration d = r.getDecoration();
		if (d instanceof ContainerDecoration) {
			setIsExcited(((ContainerDecoration)d).isContainerExcited());
		}
		// This is to update the rendered text for names (and labels) of group children,
		// which is necessary to calculate the bounding box before children have been drawn
		for (VisualComponent component: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
			component.cacheRenderedText(r);
		}

		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		if ((bb != null) && (getParent() != null)) {
			Graphics2D g = r.getGraphics();
			if (getIsCollapsed() && !isCurrentLevelInside()) {
				g.setColor(Coloriser.colorise(this.getFillColor(), d.getColorisation()));
				g.fill(bb);
			}
			float[] pattern = {0.2f, 0.2f};
			g.setStroke(new BasicStroke(0.05f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
			g.draw(bb);

			if (d.getColorisation() != null) {
				drawPivot(r);
			}

			drawNameInLocalSpace(r);
			drawLabelInLocalSpace(r);
		}
	}

}
