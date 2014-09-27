package org.workcraft.plugins.fsm.tools;

import java.awt.Color;
import java.awt.geom.Point2D;

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
import org.workcraft.plugins.fsm.VisualEvent;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
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
	public String getTraceLabelByReference(String ref) {
		String label = null;
		if (ref != null) {
			label = generator.getSymbol(ref);
			if (label == "") label = "ε";
		}
		if (label == null) {
			label = super.getTraceLabelByReference(ref);
		}
		return label;
	}

	@Override
	public VisualModel getUnderlyingModel(VisualModel model) {
		generator = new PetriNetGenerator((VisualFsm)model);
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
			if (node instanceof VisualEvent) {
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

				if (node instanceof VisualEvent) {
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

				if (node instanceof VisualState) {
					final VisualPlace p = generator.getRelatedPlace((VisualState)node);
					return new Decoration() {
						@Override
						public Color getColorisation() {
							return null;
						}
						@Override
						public Color getBackground() {
							if (p.getReferencedPlace().getTokens() > 0) return CommonSimulationSettings.getEnabledForegroundColor();
							return null;
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
		if ((node != null) && (node instanceof VisualEvent)) {
			VisualTransition vTransition = generator.getRelatedTransition((VisualEvent)node);
			if (vTransition != null) {
				Transition transition = vTransition.getReferencedTransition();
				if (net.isEnabled(transition)) {
					return transition;
				}
			}
		}
		return null;
	}

}
