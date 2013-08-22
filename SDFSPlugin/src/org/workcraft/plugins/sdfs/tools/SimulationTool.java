package org.workcraft.plugins.sdfs.tools;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.sdfs.VisualCounterflowLogic;
import org.workcraft.plugins.sdfs.VisualCounterflowRegister;
import org.workcraft.plugins.sdfs.VisualSpreadtokenLogic;
import org.workcraft.plugins.sdfs.VisualSpreadtokenRegister;
import org.workcraft.plugins.sdfs.VisualSDFS;
import org.workcraft.plugins.sdfs.decorations.CounterflowLogicDecoration;
import org.workcraft.plugins.sdfs.decorations.CounterflowRegisterDecoration;
import org.workcraft.plugins.sdfs.decorations.SpreadtokenLogicDecoration;
import org.workcraft.plugins.sdfs.decorations.SpreadtokenRegisterDecoration;
import org.workcraft.plugins.sdfs.stg.CounterflowLogicSTG;
import org.workcraft.plugins.sdfs.stg.CounterflowRegisterSTG;
import org.workcraft.plugins.sdfs.stg.STGGenerator;
import org.workcraft.plugins.sdfs.stg.SpreadtokenLogicSTG;
import org.workcraft.plugins.sdfs.stg.SpreadtokenRegisterSTG;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Func;

public class SimulationTool extends STGSimulationTool {
	private VisualSDFS sdfs;
	private GraphEditor editor;
	private STGGenerator generator;

	@Override
	public void activated(GraphEditor editor) {
		editor.getWorkspaceEntry().setCanUndoAndRedo(false);
		// editor.getWorkspaceEntry().captureMemento(); // saving-restoring memento not needed in this tool
		sdfs = (VisualSDFS)editor.getModel();
		generator = new STGGenerator(sdfs);
		visualNet = generator.getSTG();
		net = (PetriNetModel)visualNet.getMathModel();
		initialMarking = readMarking();
		traceStep = 0;
		branchTrace = null;
		branchStep = 0;
		this.editor = editor;
		initialiseStateMap();
		statusPanel.setVisible(false);
		update();
	}

	@Override
	public void deactivated(GraphEditor editor) {
		// editor.getWorkspaceEntry().cancelMemento(); // saving-restoring memento not needed in this tool
	}

	@Override
	public void update() {
		super.update();
		editor.repaint();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Point2D point = e.getPosition();
		Node node = HitMan.hitDeepest(point, e.getModel().getRoot(),
			new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {
					return (getExcitedTransitionOfNode(node) != null);
				}
			});

		Transition transition = null;
		if (node instanceof VisualCounterflowLogic) {
			VisualCounterflowLogic logic = (VisualCounterflowLogic)node;
			CounterflowLogicSTG lstg = generator.getCounterflowLogicSTG(logic);
			if (logic.getParentToLocalTransform().transform(point, null).getY() < 0) {
				transition = getExcitedTransitionOfCollection(lstg.getForwardTransitions());
			} else {
				transition = getExcitedTransitionOfCollection(lstg.getBackwardTransitions());
			}
		} else 	if (node instanceof VisualCounterflowRegister) {
			VisualCounterflowRegister register = (VisualCounterflowRegister)node;
			CounterflowRegisterSTG rstg = generator.getCounterflowRegisterSTG(register);
			if (register.getParentToLocalTransform().transform(point, null).getY() < 0) {
				transition = getExcitedTransitionOfCollection(rstg.getOrTransitions());
				if (transition == null) {
					transition = getExcitedTransitionOfCollection(rstg.getForwardTransitions());
				}
			} else {
				transition = getExcitedTransitionOfCollection(rstg.getAndTransitions());
				if (transition == null) {
					transition = getExcitedTransitionOfCollection(rstg.getBackwardTransitions());
				}
			}
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

				if (node instanceof VisualSpreadtokenLogic) {
					VisualSpreadtokenLogic logic = (VisualSpreadtokenLogic) node;
					final SpreadtokenLogicSTG lstg = generator.getSpreadtokenLogicSTG(logic);

					return new SpreadtokenLogicDecoration() {
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

						@Override
						public boolean isComputed() {
							return (lstg.C0.getTokens() == 0);
						}
					};
				}

				if (node instanceof VisualSpreadtokenRegister) {
					VisualSpreadtokenRegister register = (VisualSpreadtokenRegister) node;
					final SpreadtokenRegisterSTG rstg = generator.getSpreadtokenRegisterSTG(register);

					return new SpreadtokenRegisterDecoration() {
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

						@Override
						public boolean isMarked() {
							return (rstg.M0.getTokens() == 0);
						}

						@Override
						public boolean isEnabled() {
							return (rstg.E0.getTokens() == 0);
						}

						@Override
						public boolean isMarkedExcited() {
							return (getExcitedTransitionOfCollection(Arrays.asList(rstg.MR, rstg.MF)) != null);
						}

						@Override
						public boolean isEnabledExcited() {
							return (getExcitedTransitionOfCollection(Arrays.asList(rstg.ER, rstg.EF)) != null);
						}
					};
				}

				if (node instanceof VisualCounterflowLogic) {
					VisualCounterflowLogic logic = (VisualCounterflowLogic) node;
					final CounterflowLogicSTG lstg = generator.getCounterflowLogicSTG(logic);

					return new CounterflowLogicDecoration() {
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

						@Override
						public boolean isForwardComputed() {
							return (lstg.fwC0.getTokens() == 0);
						}

						@Override
						public boolean isBackwardComputed() {
							return (lstg.bwC0.getTokens() == 0);
						}

						@Override
						public boolean isForwardComputedExcited() {
							return (getExcitedTransitionOfCollection(lstg.getForwardTransitions()) != null);
						}

						@Override
						public boolean isBackwardComputedExcited() {
							return (getExcitedTransitionOfCollection(lstg.getBackwardTransitions()) != null);
						}
					};
				}

				if (node instanceof VisualCounterflowRegister) {
					VisualCounterflowRegister register = (VisualCounterflowRegister) node;
					final CounterflowRegisterSTG rstg = generator.getCounterflowRegisterSTG(register);

					return new CounterflowRegisterDecoration() {
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

						@Override
						public boolean isForwardEnabled() {
							return (rstg.fwE0.getTokens() == 0);
						}

						@Override
						public boolean isBackwardEnabled() {
							return (rstg.bwE0.getTokens() == 0);
						}

						@Override
						public boolean isOrMarked() {
							return (rstg.orM0.getTokens() == 0);
						}

						@Override
						public boolean isAndMarked() {
							return (rstg.andM0.getTokens() == 0);
						}

						@Override
						public boolean isForwardEnabledExcited() {
							return (getExcitedTransitionOfCollection(rstg.getForwardTransitions()) != null);
						}

						@Override
						public boolean isBackwardEnabledExcited() {
							return (getExcitedTransitionOfCollection(rstg.getBackwardTransitions()) != null);
						}

						@Override
						public boolean isOrMarkedExcited() {
							return (getExcitedTransitionOfCollection(rstg.getOrTransitions()) != null);
						}

						@Override
						public boolean isAndMarkedExcited() {
							return (getExcitedTransitionOfCollection(rstg.getAndTransitions()) != null);
						}
					};
				}

				return null;
			}
		};
	}

	private Transition getExcitedTransitionOfNode(Node node) {
		List<VisualSignalTransition> ts = null;
		if (node != null) {
			if (node instanceof VisualSpreadtokenLogic) {
				VisualSpreadtokenLogic logic = (VisualSpreadtokenLogic) node;
				ts = generator.getSpreadtokenLogicSTG(logic).getAllTransitions();
			} else if (node instanceof VisualSpreadtokenRegister) {
				VisualSpreadtokenRegister register = (VisualSpreadtokenRegister) node;
				ts = generator.getSpreadtokenRegisterSTG(register).getAllTransitions();
			} else if (node instanceof VisualCounterflowLogic) {
				VisualCounterflowLogic logic = (VisualCounterflowLogic) node;
				ts = generator.getCounterflowLogicSTG(logic).getAllTransitions();
			} else if (node instanceof VisualCounterflowRegister) {
				VisualCounterflowRegister register = (VisualCounterflowRegister) node;
				ts = generator.getCounterflowRegisterSTG(register).getAllTransitions();
			}
		}
		return getExcitedTransitionOfCollection(ts);
	}

	private Transition getExcitedTransitionOfCollection(List<VisualSignalTransition> ts) {
		if (ts != null) {
			for (VisualSignalTransition t: ts) {
				Transition transition = t.getReferencedTransition();
				if (net.isEnabled(transition)) {
					return transition;
				}
			}
		}
		return null;
	}

}
