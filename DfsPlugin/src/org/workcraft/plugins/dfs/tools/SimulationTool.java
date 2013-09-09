package org.workcraft.plugins.dfs.tools;

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
import org.workcraft.plugins.dfs.VisualControlRegister;
import org.workcraft.plugins.dfs.VisualCounterflowLogic;
import org.workcraft.plugins.dfs.VisualCounterflowRegister;
import org.workcraft.plugins.dfs.VisualLogic;
import org.workcraft.plugins.dfs.VisualPopRegister;
import org.workcraft.plugins.dfs.VisualPushRegister;
import org.workcraft.plugins.dfs.VisualRegister;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.decorations.BinaryRegisterDecoration;
import org.workcraft.plugins.dfs.decorations.CounterflowLogicDecoration;
import org.workcraft.plugins.dfs.decorations.CounterflowRegisterDecoration;
import org.workcraft.plugins.dfs.decorations.LogicDecoration;
import org.workcraft.plugins.dfs.decorations.RegisterDecoration;
import org.workcraft.plugins.dfs.stg.BinaryRegisterStg;
import org.workcraft.plugins.dfs.stg.CounterflowLogicStg;
import org.workcraft.plugins.dfs.stg.CounterflowRegisterStg;
import org.workcraft.plugins.dfs.stg.LogicStg;
import org.workcraft.plugins.dfs.stg.RegisterStg;
import org.workcraft.plugins.dfs.stg.StgGenerator;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Func;

public class SimulationTool extends STGSimulationTool {
	private VisualDfs dfs;
	private GraphEditor editor;
	private StgGenerator generator;

	@Override
	public void activated(GraphEditor editor) {
		editor.getWorkspaceEntry().setCanUndoAndRedo(false);
		// editor.getWorkspaceEntry().captureMemento(); // saving-restoring memento not needed in this tool
		dfs = (VisualDfs)editor.getModel();
		generator = new StgGenerator(dfs);
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
			CounterflowLogicStg lstg = generator.getCounterflowLogicSTG(logic);
			if (logic.getParentToLocalTransform().transform(point, null).getY() < 0) {
				transition = getExcitedTransitionOfCollection(lstg.getForwardTransitions());
			} else {
				transition = getExcitedTransitionOfCollection(lstg.getBackwardTransitions());
			}
		} else if (node instanceof VisualCounterflowRegister) {
			VisualCounterflowRegister register = (VisualCounterflowRegister)node;
			CounterflowRegisterStg rstg = generator.getCounterflowRegisterSTG(register);
			if (register.getParentToLocalTransform().transform(point, null).getY() < 0) {
				transition = getExcitedTransitionOfCollection(rstg.getOrTransitions());
			} else {
				transition = getExcitedTransitionOfCollection(rstg.getAndTransitions());
			}
		} else if (node instanceof VisualControlRegister) {
			VisualControlRegister register = (VisualControlRegister)node;
			BinaryRegisterStg rstg = generator.getControlRegisterSTG(register);
			if (register.getParentToLocalTransform().transform(point, null).getY() < 0) {
				transition = getExcitedTransitionOfCollection(rstg.getTrueTransitions());
			} else {
				transition = getExcitedTransitionOfCollection(rstg.getFalseTransitions());
			}
		} else if (node instanceof VisualPushRegister) {
			VisualPushRegister register = (VisualPushRegister)node;
			BinaryRegisterStg rstg = generator.getPushRegisterSTG(register);
			if (register.getParentToLocalTransform().transform(point, null).getY() < 0) {
				transition = getExcitedTransitionOfCollection(rstg.getTrueTransitions());
			} else {
				transition = getExcitedTransitionOfCollection(rstg.getFalseTransitions());
			}
		} else if (node instanceof VisualPopRegister) {
			VisualPopRegister register = (VisualPopRegister)node;
			BinaryRegisterStg rstg = generator.getPopRegisterSTG(register);
			if (register.getParentToLocalTransform().transform(point, null).getY() < 0) {
				transition = getExcitedTransitionOfCollection(rstg.getTrueTransitions());
			} else {
				transition = getExcitedTransitionOfCollection(rstg.getFalseTransitions());
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

				if (node instanceof VisualLogic) {
					final LogicStg lstg = generator.getLogicSTG((VisualLogic) node);

					return new LogicDecoration() {
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

				if (node instanceof VisualRegister) {
					final RegisterStg rstg = generator.getRegisterSTG((VisualRegister) node);

					return new RegisterDecoration() {
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
						public boolean isExcited() {
							return (getExcitedTransitionOfCollection(Arrays.asList(rstg.MR, rstg.MF)) != null);
						}
					};
				}

				if (node instanceof VisualCounterflowLogic) {
					final CounterflowLogicStg lstg = generator.getCounterflowLogicSTG((VisualCounterflowLogic) node);

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
					final CounterflowRegisterStg rstg = generator.getCounterflowRegisterSTG((VisualCounterflowRegister) node);

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
						public boolean isForwardExcited() {
							return (getExcitedTransitionOfCollection(rstg.getForwardTransitions()) != null);
						}

						@Override
						public boolean isBackwardExcited() {
							return (getExcitedTransitionOfCollection(rstg.getBackwardTransitions()) != null);
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
						public boolean isOrMarkedExcited() {
							return (getExcitedTransitionOfCollection(rstg.getOrTransitions()) != null);
						}

						@Override
						public boolean isAndMarkedExcited() {
							return (getExcitedTransitionOfCollection(rstg.getAndTransitions()) != null);
						}
					};
				}

				if (node instanceof VisualControlRegister || node instanceof VisualPushRegister || node instanceof VisualPopRegister) {
					BinaryRegisterStg tmp = null;
					if (node instanceof VisualControlRegister) {
						tmp = generator.getControlRegisterSTG((VisualControlRegister) node);
					}
					if (node instanceof VisualPushRegister) {
						tmp = generator.getPushRegisterSTG((VisualPushRegister) node);
					}
					if (node instanceof VisualPopRegister) {
						tmp = generator.getPopRegisterSTG((VisualPopRegister) node);
					}
					final BinaryRegisterStg rstg = tmp;

					return new BinaryRegisterDecoration() {
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
						public boolean isTrueMarked() {
							return (rstg.tM0.getTokens() == 0);
						}

						@Override
						public boolean isTrueExcited() {
							return (getExcitedTransitionOfCollection(Arrays.asList(rstg.tMR, rstg.tMF)) != null);
						}

						@Override
						public boolean isFalseMarked() {
							return (rstg.fM0.getTokens() == 0);
						}

						@Override
						public boolean isFalseExcited() {
							return (getExcitedTransitionOfCollection(Arrays.asList(rstg.fMR, rstg.fMF)) != null);
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
			if (node instanceof VisualLogic) {
				ts = generator.getLogicSTG((VisualLogic) node).getAllTransitions();
			} else if (node instanceof VisualRegister) {
				ts = generator.getRegisterSTG((VisualRegister) node).getAllTransitions();
			} else if (node instanceof VisualCounterflowLogic) {
				ts = generator.getCounterflowLogicSTG((VisualCounterflowLogic) node).getAllTransitions();
			} else if (node instanceof VisualCounterflowRegister) {
				ts = generator.getCounterflowRegisterSTG((VisualCounterflowRegister) node).getAllTransitions();
			} else if (node instanceof VisualControlRegister) {
				ts = generator.getControlRegisterSTG((VisualControlRegister) node).getAllTransitions();
			} else if (node instanceof VisualPushRegister) {
				ts = generator.getPushRegisterSTG((VisualPushRegister) node).getAllTransitions();
			} else if (node instanceof VisualPopRegister) {
				ts = generator.getPopRegisterSTG((VisualPopRegister) node).getAllTransitions();
			}
		}
		return getExcitedTransitionOfCollection(ts);
	}

	private Transition getExcitedTransitionOfCollection(List<VisualSignalTransition> ts) {
		if (ts != null) {
			for (VisualSignalTransition t: ts) {
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
