/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.workcraft.NodeFactory;
import org.workcraft.annotations.MouseListeners;
import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.DefaultMathNodeRemover;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.DefaultAnchorGenerator;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.util.Hierarchy;

@MouseListeners ({ DefaultAnchorGenerator.class })
public abstract class AbstractVisualModel extends AbstractModel implements VisualModel {
	private MathModel mathModel;
	private Container currentLevel;
	private Set<Node> selection = new HashSet<Node>();
	private ObservableStateImpl observableState = new ObservableStateImpl();

	public AbstractVisualModel() {
		this(null, null);
	}

	public AbstractVisualModel(MathModel mathModel) {
		this(mathModel, null);
	}

	public AbstractVisualModel(MathModel mathModel, VisualGroup root) {
		super((root == null) ? new VisualGroup() : root);
		this.mathModel = mathModel;
		this.currentLevel = (VisualGroup)getRoot();
		new TransformEventPropagator().attach(getRoot());
		new SelectionEventPropagator(this).attach(getRoot());
		new RemovedNodeDeselector(this).attach(getRoot());
		new DefaultHangingConnectionRemover(this, "Visual").attach(getRoot());
		new DefaultMathNodeRemover().attach(getRoot());

		new StateSupervisor() {
			@Override
			public void handleHierarchyEvent(HierarchyEvent e) {
				observableState.sendNotification(new ModelModifiedEvent(AbstractVisualModel.this));
			}
			@Override
			public void handleEvent(StateEvent e) {
				observableState.sendNotification(new ModelModifiedEvent(AbstractVisualModel.this));
			}
		}.attach(getRoot());
	}

	protected final void createDefaultFlatStructure() throws NodeCreationException {
		HashMap <MathNode, VisualComponent> createdNodes = new HashMap <MathNode, VisualComponent>();
		HashMap <VisualConnection, MathConnection> createdConnections = new	HashMap <VisualConnection, MathConnection>();

		for (Node n : mathModel.getRoot().getChildren()) {
			if (n instanceof MathConnection) {
				MathConnection connection = (MathConnection)n;

				VisualConnection visualConnection = NodeFactory.createVisualConnection(connection);
				if (visualConnection != null) {
					// Will create incomplete instance, setConnection() needs to be called later to finalise.
					// This is to avoid cross-reference problems.
					createdConnections.put(visualConnection, connection);
				}
			} else {
				MathNode node = (MathNode)n;
				VisualComponent visualComponent = (VisualComponent)NodeFactory.createVisualComponent(node);

				if (visualComponent != null) {
					getRoot().add(visualComponent);
					createdNodes.put(node, visualComponent);
				}
			}
		}

		for (VisualConnection vc : createdConnections.keySet()) {
			MathConnection mc = createdConnections.get(vc);
			vc.setVisualConnectionDependencies(
					createdNodes.get(mc.getFirst()),
					createdNodes.get(mc.getSecond()),
					new Polyline(vc), mc);

			getRoot().add(vc);
//			if (mc.getFirst() == mc.getSecond()) {
//				vc.setConnectionType(ConnectionType.BEZIER);
//			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends VisualComponent> T createComponent(MathNode mathNode, Container container, Class<T> type) {
		if (container == null) {
			container = getRoot();
		}
		VisualComponent component = null;
		try {
			component = NodeFactory.createVisualComponent(mathNode);
			container.add(component);
		} catch (NodeCreationException e) {
			String mathName = getMathName(mathNode);
			throw new RuntimeException ("Cannot create visual component for math node \"" + mathName + "\" of class \"" + type +"\"");
		}
		return (T)component;
	}


	public void draw (Graphics2D g, Decorator decorator) {
		DrawMan.draw(this, g, decorator, getRoot());
	}

	/**
	 * Get the list of selected objects. Returned list is modifiable!
	 * @return the selection.
	 */
	public Set<Node> selection() {
		return selection;
	}

	private Collection<Node> saveSelection() {
		Set<Node> prevSelection = new HashSet<Node>();
		prevSelection.addAll(selection);
		return prevSelection;
	}

	private void notifySelectionChanged(Collection<Node> prevSelection) {
		sendNotification(new SelectionChangedEvent(this, prevSelection));
	}

	/**
	 * Select all components, connections and groups from the <code>root</code> group.
	 */
	@Override
	public void selectAll() {
		if(selection.size() == getCurrentLevel().getChildren().size()) {
			return;
		}
		Collection<Node> s = saveSelection();
		selection.clear();
		selection.addAll(getCurrentLevel().getChildren());
		notifySelectionChanged(s);
	}

	/**
	 * Clear selection.
	 */
	@Override
	public void selectNone() {
		if (!selection.isEmpty()) {
			Collection<Node> s = saveSelection();
			selection.clear();
			notifySelectionChanged(s);
		}
	}

	/**
	 * Invert selection.
	 */
	@Override
	public void selectInverse() {
		Collection<Node> s = saveSelection();
		selection.clear();
		for (Node node: getCurrentLevel().getChildren()) {
			if (!s.contains(node)) {
				selection.add(node);
			}
		}
		notifySelectionChanged(getCurrentLevel().getChildren());
	}

	private void validateSelection (Node node) {
		if (!Hierarchy.isDescendant(node, getCurrentLevel())) {
			throw new RuntimeException (
				"Cannot select a node that is not in the current editing level ("
				+ node + "), parent (" + node.getParent() +")");
		}
	}

	private void validateSelection (Collection<Node> nodes) {
		for (Node node : nodes)
			validateSelection(node);
	}

	public boolean isSelected(Node node) {
		return selection.contains(node);
	}

	public void select(Collection<Node> nodes) {
		if (nodes.isEmpty()) {
			selectNone();
			return;
		}
		Collection<Node> s = saveSelection();
		validateSelection(nodes);
		selection.clear();
		selection.addAll(nodes);
		notifySelectionChanged(s);
	}

	@Override
	public void select(Node node) {
		if (selection.size() == 1 && selection.contains(node)) {
			return;
		}
		Collection<Node> s = saveSelection();
		validateSelection(node);
		selection.clear();
		selection.add(node);
		notifySelectionChanged(s);
	}

	@Override
	public void addToSelection(Node node) {
		if (selection.contains(node)) {
			return;
		}
		Collection<Node> s = saveSelection();
		validateSelection(node);
		selection.add(node);
		notifySelectionChanged(s);
	}

	@Override
	public void addToSelection(Collection<Node> nodes) {
		Collection<Node> s = saveSelection();
		validateSelection(nodes);
		selection.addAll(nodes);
		if (s.size() != selection.size()) {
			notifySelectionChanged(s);
		}
	}

	@Override
	public void removeFromSelection(Node node) {
		if (selection.contains(node)) {
			Collection<Node> s = saveSelection();
			selection.remove(node);
			notifySelectionChanged(s);
		}
	}

	@Override
	public void removeFromSelection(Collection<Node> nodes) {
		Collection<Node> s = saveSelection();
		selection.removeAll(nodes);
		if (s.size() != selection.size()) {
			notifySelectionChanged(s);
		}
	}

	@Override
	public MathModel getMathModel() {
		return mathModel;
	}

	@Override
	public String getNodeMathReference(Node node) {
		if (node instanceof VisualComponent) {
			VisualComponent component = (VisualComponent)node;
			node = component.getReferencedComponent();
		}
		return getMathModel().getNodeReference(node);
	}

	@Override
	public String getMathName(Node node) {
		if (node instanceof VisualComponent) {
			VisualComponent component = (VisualComponent)node;
			node = component.getReferencedComponent();
		}
		return getMathModel().getName(node);
	}

	@Override
	public void setMathName(Node node, String name) {
		if (node instanceof VisualComponent) {
			VisualComponent component = (VisualComponent)node;
			node = component.getReferencedComponent();
		}
		getMathModel().setName(node, name);
	}

	public static Point2D centralizeComponents(Collection<Node> components) {
		// Find weighted center
		double deltaX = 0.0;
		double deltaY = 0.0;
		int num = 0;
		for (Node node: components) {
			if (node instanceof VisualTransformableNode) {
				VisualTransformableNode tn = (VisualTransformableNode)node;
				deltaX += tn.getX();
				deltaY += tn.getY();
				num++;
			}
		}
		if (num>0) {
			deltaX /= num;
			deltaY /= num;
		}
		// Round numbers
		deltaX = Math.round(deltaX*2)/2;
		deltaY = Math.round(deltaY*2)/2;

		// Move components
		for (Node node: components) {
			if (node instanceof VisualTransformableNode && !(node instanceof ControlPoint)) {
				VisualTransformableNode tn = (VisualTransformableNode)node;
				tn.setPosition(new Point2D.Double(tn.getX() - deltaX, tn.getY() - deltaY));
			}
		}
		return new Point2D.Double(deltaX, deltaY);
	}

	@Override
	public Container getCurrentLevel() {
		return currentLevel;
	}

	@Override
	public void setCurrentLevel(Container newCurrentLevel) {
		selection.clear();
		currentLevel = newCurrentLevel;

		// manage the isInside value for all parents and children
		Collapsible col = null;
		if (newCurrentLevel instanceof Collapsible) {
			col = (Collapsible)newCurrentLevel;
		}

		if (col!=null) {
			col.setIsCurrentLevelInside(true);
			Node node = newCurrentLevel.getParent();
			while (node!=null) {
				if ((node instanceof Collapsible))
					((Collapsible)node).setIsCurrentLevelInside(true);
				node = node.getParent();
			}

			for (Node n: newCurrentLevel.getChildren()) {
				if (!(n instanceof Collapsible)) continue;
				((Collapsible)n).setIsCurrentLevelInside(false);
			}
		}
	}

	private Container getCurrentMathLevel() {
		Container currentMathLevel;
		VisualComponent visualContainer = (VisualComponent)Hierarchy.getNearestAncestor(getCurrentLevel(), VisualComponent.class);
		if (visualContainer == null) {
			currentMathLevel = getMathModel().getRoot();
		} else {
			currentMathLevel = (Container)visualContainer.getReferencedComponent();
		}
		return currentMathLevel;
	}

	/**
	 * @return Returns selection ordered the same way as the objects are ordered in the currently active group.
	 */
	@Override
	public Collection<Node> getSelection() {
		return Collections.unmodifiableSet(selection);
	}

	@Override
	public boolean isGroupable(Node node) {
		return (node instanceof VisualNode);
	}

	@Override
	public VisualGroup groupSelection() {
		VisualGroup group = null;
		Collection<Node> nodes = SelectionHelper.getGroupableCurrentLevelSelection(this);
		if (nodes.size() >= 1) {
			group = new VisualGroup();
			getCurrentLevel().add(group);
			getCurrentLevel().reparent(nodes, group);
			group.setPosition(centralizeComponents(nodes));
			select(group);
		}
		return group;
	}

	@Override
	public VisualPage groupPageSelection() {
		VisualPage page = null;
		Collection<Node> nodes = SelectionHelper.getGroupableCurrentLevelSelection(this);
		if (nodes.size() >= 1) {
			PageNode pageNode = new PageNode();
			getCurrentMathLevel().add(pageNode);
			page = new VisualPage(pageNode);
			getCurrentLevel().add(page);
			reparent(page, this, getCurrentLevel(), nodes);
			page.setPosition(centralizeComponents(nodes));
			select(page);
		}
		return page;
	}

	@Override
	public void ungroupSelection() {
		ArrayList<Node> toSelect = new ArrayList<Node>();
		for(Node node : SelectionHelper.getOrderedCurrentLevelSelection(this)) {
			if (node instanceof VisualGroup) {
				VisualGroup group = (VisualGroup)node;
				for(Node subNode : group.unGroup()) {
					toSelect.add(subNode);
				}
				getCurrentLevel().remove(group);
			} else if (node instanceof VisualPage) {
				VisualPage page = (VisualPage)node;
				ArrayList<Node> nodesToReparent = new ArrayList<Node>(page.getChildren());
				toSelect.addAll(nodesToReparent);
				this.reparent(getCurrentLevel(), this, page, nodesToReparent);
				AffineTransform localToParentTransform = page.getLocalToParentTransform();
				for (Node n : nodesToReparent) {
					TransformHelper.applyTransform(n, localToParentTransform);
				}
				getMathModel().remove(page.getReferencedComponent());
				getCurrentLevel().remove(page);
			} else {
				toSelect.add(node);
			}
		}
		select(toSelect);
	}


	@Override
	public void ungroupPageSelection() {
		ungroupSelection();
	}

	@Override
	public void deleteSelection() {
		HashMap<Container, LinkedList<Node>> batches = new HashMap<Container, LinkedList<Node>>();
		LinkedList<Node> remainingSelection = new LinkedList<Node>();

		for (Node n : selection) {
			if (n.getParent() instanceof Container) {
				Container c = (Container)n.getParent();
				LinkedList<Node> batch = batches.get(c);
				if (batch == null) {
					batch = new LinkedList<Node>();
					batches.put(c, batch);
				}
				batch.add(n);
			} else remainingSelection.add(n);
		}

		for (Container c : batches.keySet()) {
			c.remove(batches.get(c));
		}
		select(remainingSelection);
	}

	private Point2D transformToCurrentSpace(Point2D pointInRootSpace) {
		Point2D newPoint = new Point2D.Double();
		TransformHelper.getTransform(getRoot(), currentLevel).transform(pointInRootSpace, newPoint);
		return newPoint;
	}

	@Override
	public Collection<Node> boxHitTest(Point2D p1, Point2D p2) {
		p1 = transformToCurrentSpace(p1);
		p2 = transformToCurrentSpace(p2);
		return HitMan.boxHitTest(currentLevel, p1, p2);
	}

	@Override
	public void addObserver(StateObserver obs) {
		observableState.addObserver(obs);
	}

	@Override
	public void removeObserver(StateObserver obs) {
		observableState.removeObserver(obs);
	}

	public void sendNotification(StateEvent e) {
		observableState.sendNotification(e);
	}

	@Override
	public ModelProperties getProperties(Node node) {
		return new ModelProperties();
	}

	public static Collection<Node> getMathChildren(Collection<Node> nodes) {
		Collection<Node> ret = new HashSet<Node>();
		for (Node node: nodes) {
			if (node instanceof DependentNode) {
				ret.addAll( ((DependentNode)node).getMathReferences());
			} else if (node instanceof VisualGroup) {
				ret.addAll(getMathChildren(node.getChildren()));
			}
		}
		return ret;
	}

	@Override
	public void reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren) {
		if (srcModel == null) {
			srcModel = this;
		}
		if (srcChildren == null) {
			srcChildren = srcRoot.getChildren();
		}

		Container srcMathContainer = NamespaceHelper.getMathContainer((VisualModel)srcModel, srcRoot);
		Collection<Node> srcMathChildren = getMathChildren(srcChildren);
		MathModel srcMathModel = ((VisualModel)srcModel).getMathModel();

		MathModel dstMathMmodel = getMathModel();
		Container dstMathContainer = NamespaceHelper.getMathContainer(this, dstContainer);
		dstMathMmodel.reparent(dstMathContainer, srcMathModel, srcMathContainer, srcMathChildren);

		Collection<Node> dstChildren = new HashSet<Node>();
		dstChildren.addAll(srcChildren);
		srcRoot.reparent(dstChildren, dstContainer);
	}

}
