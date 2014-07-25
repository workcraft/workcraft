package org.workcraft.plugins.policy.tools;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ContainerDecoration;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
import org.workcraft.plugins.petri.tools.PlaceDecoration;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.util.Func;

public class SimulationTool extends PetriNetSimulationTool {
	private PetriNetGenerator generator;

	@Override
	public void activated(final GraphEditor editor) {
		super.activated(editor);
		statusPanel.setVisible(false);
	}

	@Override
	public VisualModel getUnderlyingModel(VisualModel model) {
		generator = new PetriNetGenerator((VisualPolicyNet)model);
		return generator.getPetriNet();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Point2D posRoot = e.getPosition();
		Node node = HitMan.hitDeepest(posRoot, e.getModel().getRoot(),
			new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {
					return (getExcitedTransitionOfNode(node) != null);
				}
			});

		Transition transition = null;
		if (node instanceof VisualTransformableNode) {
		}

		if (transition == null) {
			transition = getExcitedTransitionOfNode(node);
		}

		if (transition != null) {
			executeTransition(e.getEditor(), transition);
		}
	}

	protected boolean isContainerExcited(Container container) {
		if (excitedContainers.containsKey(container)) return excitedContainers.get(container);
		boolean ret = false;

		for (Node node: container.getChildren()) {

			if (node instanceof VisualBundledTransition) {
				ret=ret || (getExcitedTransitionOfNode(node) != null);
			}

			if (node instanceof Container) {
				ret = ret || isContainerExcited((Container)node);
			}

			if (ret) break;
		}

		excitedContainers.put(container, ret);
		return ret;
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				String transitionId = null;
				Node transition = null;
				if (branchTrace.canProgress()) {
					transitionId = branchTrace.getCurrent();
					transition = net.getNodeByReference(transitionId);
				} else if (branchTrace.isEmpty() && mainTrace.canProgress()) {
					transitionId = mainTrace.getCurrent();
					transition = net.getNodeByReference(transitionId);
				}

				final boolean isExcited = (getExcitedTransitionOfNode(node) != null);
				final boolean isHighlighted = generator.isRelated(node, transition);

				if (node instanceof VisualBundledTransition) {
					return new Decoration() {
						@Override
						public Color getColorisation() {
							if (isHighlighted) return CommonSimulationSettings.getEnabledBackgroundColor();
							if (isExcited) return CommonSimulationSettings.getEnabledForegroundColor();
							return null;
						}

						@Override
						public Color getBackground() {
							if (isHighlighted) return CommonSimulationSettings.getEnabledForegroundColor();
							if (isExcited) return CommonSimulationSettings.getEnabledBackgroundColor();
							return null;
						}
					};
				}

				if (node instanceof VisualPlace) {
					final VisualPlace p = generator.getRelatedPlace((VisualPlace)node);
					return new PlaceDecoration() {
						@Override
						public Color getColorisation() {
							return null;
						}
						@Override
						public Color getBackground() {
							return null;
						}
						@Override
						public int getTokens() {
							return (p == null ? 0 : p.getReferencedPlace().getTokens());
						}
						@Override
						public Color getTokenColor() {
							return p.getTokenColor();
						}
					};
				}

				if (node instanceof VisualPage || node instanceof VisualGroup) {

					if (node.getParent()==null) return null; // do not work with the root node

					final boolean ret = isContainerExcited((Container)node);

					return new ContainerDecoration() {

						@Override
						public Color getColorisation() {
							return null;
						}

						@Override
						public Color getBackground() {
							return null;
						}

						@Override
						public boolean isContainerExcited() {
							return ret;
						}
					};

				}


				return null;
			}
		};
	}

	private Transition getExcitedTransitionOfNode(Node node) {
		Collection<VisualTransition> ts = null;
		if (node != null && node instanceof VisualBundledTransition) {
			ts = generator.getRelatedTransitions((VisualBundledTransition)node);
		}
		return getExcitedTransitionOfCollection(ts);
	}

	private Transition getExcitedTransitionOfCollection(Collection<VisualTransition> ts) {
		if (ts != null) {
			for (VisualTransition t: ts) {
				if (t == null) continue;
				Transition transition = t.getReferencedTransition();
				if (net.isEnabled(transition)) {
					return transition;
				}
			}
		}
		return null;
	}

}
