package org.workcraft.plugins.sdfs.tools;

import java.awt.Color;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.sdfs.VisualLogic;
import org.workcraft.plugins.sdfs.VisualRegister;
import org.workcraft.plugins.sdfs.VisualSDFS;
import org.workcraft.plugins.shared.CommonVisualSettings;
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
		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
			new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {
					return (getExcitedTransition(node) != null);
				}
			});

		Transition transiton = getExcitedTransition(node);
		if (transiton != null)
			executeTransition(transiton);
	}

	@Override
	public Decorator getDecorator() {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				String transitionId = null;
				Node transition = null;
				if (branchTrace != null && branchStep<branchTrace.size()) {
					transitionId = branchTrace.get(branchStep);
					transition = net.getNodeByReference(transitionId);
				} else if (branchTrace == null && trace != null&&traceStep<trace.size()) {
					transitionId = trace.get(traceStep);
					transition = net.getNodeByReference(transitionId);
				}

				final boolean isExcited = (getExcitedTransition(node) != null);
				final boolean isHighlighted = generator.isRelated(node, transition);

				if (node instanceof VisualLogic) {
					VisualLogic logic = (VisualLogic) node;
					final LogicSTG lstg = generator.getLogicSTG(logic);

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
							return (lstg.c0.getTokens() == 0);
						}
					};
				}

				if (node instanceof VisualRegister) {
					VisualRegister register = (VisualRegister) node;
					final RegisterSTG rstg = generator.getRegisterSTG(register);

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
							return (rstg.m0.getTokens() == 0);
						}

						@Override
						public boolean isEnabled() {
							return (rstg.e0.getTokens() == 0);
						}
					};
				}

				return null;
			}
		};
}

	private Transition getExcitedTransition(VisualLogic logic) {
		LogicSTG lstg = generator.getLogicSTG(logic);
		if (lstg != null) {
			Transition tf = lstg.cf.getReferencedTransition();
			if (net.isEnabled(tf)) return tf;

			Transition tr = lstg.cr.getReferencedTransition();
			if (net.isEnabled(tr)) return tr;
		}
		return null;
	}

	protected Transition getExcitedTransition(VisualRegister register) {
		RegisterSTG rstg = generator.getRegisterSTG(register);
		if (rstg != null) {
			Transition tef = rstg.ef.getReferencedTransition();
			if (net.isEnabled(tef)) return tef;

			Transition ter = rstg.er.getReferencedTransition();
			if (net.isEnabled(ter)) return ter;

			Transition tmf = rstg.mf.getReferencedTransition();
			if (net.isEnabled(tmf)) return tmf;

			Transition tmr = rstg.mr.getReferencedTransition();
			if (net.isEnabled(tmr)) return tmr;
		}
		return null;
	}

	private Transition getExcitedTransition(Node node) {
		Transition result = null;
		if (node != null) {
			if (node instanceof VisualLogic) {
				result = getExcitedTransition((VisualLogic)node);
			} else if (node instanceof VisualRegister) {
				result = getExcitedTransition((VisualRegister)node);
			}
		}
		return result;
	}
}
