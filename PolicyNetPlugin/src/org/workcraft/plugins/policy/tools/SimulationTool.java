package org.workcraft.plugins.policy.tools;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
import org.workcraft.plugins.petri.tools.PlaceDecoration;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Func;

public class SimulationTool extends PetriNetSimulationTool {
	private VisualPolicyNet policy;
	private GraphEditor editor;
	private PetriNetGenerator generator;

	@Override
	public void activated(GraphEditor editor) {
		editor.getWorkspaceEntry().setCanModify(false);
		policy = (VisualPolicyNet)editor.getModel();
		generator = new PetriNetGenerator(policy);
		visualNet = generator.getPetriNet();
		net = (PetriNetModel)visualNet.getMathModel();
		initialMarking = readMarking();
		traceStep = 0;
		branchTrace = null;
		branchStep = 0;
		this.editor = editor;
		statusPanel.setVisible(false);
		update();
	}

	@Override
	public void deactivated(GraphEditor editor) {
	}

	@Override
	public void update() {
		super.update();
		editor.repaint();
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
			executeTransition(transition);
		}
	}

	@Override
	public Decorator getDecorator() {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				String transitionId = null;
				Node transition = null;
				if (branchTrace != null && branchStep < branchTrace.size()) {
					transitionId = branchTrace.get(branchStep);
					transition = net.getNodeByReference(transitionId);
				} else if (branchTrace == null && trace != null && traceStep < trace.size()) {
					transitionId = trace.get(traceStep);
					transition = net.getNodeByReference(transitionId);
				}

				final boolean isExcited = (getExcitedTransitionOfNode(node) != null);
				final boolean isHighlighted = generator.isRelated(node, transition);

				if (node instanceof VisualBundledTransition) {
					return new Decoration() {
						@Override
						public Color getColorisation() {
							if (isHighlighted) return CommonVisualSettings.getEnabledBackgroundColor();
							if (isExcited) return CommonVisualSettings.getEnabledForegroundColor();
							return null;
						}

						@Override
						public Color getBackground() {
							if (isHighlighted) return CommonVisualSettings.getEnabledForegroundColor();
							if (isExcited) return CommonVisualSettings.getEnabledBackgroundColor();
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
							return (p == null ? 0 : p.getTokens());
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
