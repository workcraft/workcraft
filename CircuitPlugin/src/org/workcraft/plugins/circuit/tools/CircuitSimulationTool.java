package org.workcraft.plugins.circuit.tools;

import java.awt.Color;

import org.workcraft.Trace;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ContainerDecoration;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.circuit.stg.SignalStg;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.util.Func;

public class CircuitSimulationTool extends StgSimulationTool {
	private CircuitToStgConverter converter;

	@Override
	public VisualModel getUnderlyingModel(VisualModel model) {
		VisualCircuit circuit = (VisualCircuit)model;
		converter = new CircuitToStgConverter(circuit);
		return converter.getStg();
	}

	@Override
	public void setTrace(Trace mainTrace, Trace branchTrace, GraphEditor editor) {
		Trace circuitMainTrace = convertStgTraceToCircuitTrace(mainTrace);
		if (circuitMainTrace != null) {
			System.out.println("Main trace convertion:");
			System.out.println("  original: " + mainTrace);
			System.out.println("  circuit:  " + circuitMainTrace);
		}
		Trace circuitBranchTrace = convertStgTraceToCircuitTrace(branchTrace);
		if (circuitBranchTrace != null) {
			System.out.println("Branch trace convertion:");
			System.out.println("  original: " + branchTrace);
			System.out.println("  circuit:  " + circuitBranchTrace);
		}
		super.setTrace(circuitMainTrace, circuitBranchTrace, editor);
	}

	private Trace convertStgTraceToCircuitTrace(Trace trace) {
		Trace circuitTrace = null;
		if (trace != null) {
			circuitTrace = new Trace();
			for (String ref: trace) {
				Transition t = getBestTransitionToFire(ref);
				if (t != null) {
					String circuitRef = net.getNodeReference(t);
					circuitTrace.add(circuitRef);
					net.fire(t);
				}
			}
			resetMarking();
		}
		return circuitTrace;
	}

	private Transition getBestTransitionToFire(String ref) {
		Transition result = null;
		if (ref != null) {
			String requiredId = LabelParser.parseInstancedTransition(ref).getFirst();
			for (Transition transition: net.getTransitions()) {
				String existingRef = net.getNodeReference(transition);
				String exisitingId = LabelParser.parseInstancedTransition(existingRef).getFirst();
				if (requiredId.equals(exisitingId) && net.isEnabled(transition)) {
					result = transition;
				}
			}
		}
		return result;
	}

	@Override
	public void applyInitState(final GraphEditor editor) {
		if ((savedState == null) || savedState.isEmpty()) {
			return;
		}
		VisualCircuit circuit = (VisualCircuit)editor.getModel();
		for (VisualFunctionContact contact : circuit.getVisualFunctionContacts()) {
			String ref = circuit.getNodeMathReference(contact) + CircuitToStgConverter.NAME_SUFFIX_1;
			Node node = net.getNodeByReference(ref);
			if ((node instanceof Place) && savedState.containsKey(node)) {
				boolean initToOne = (savedState.get(node) > 0);
				contact.getReferencedContact().setInitToOne(initToOne);
			}
		}
	}

	@Override
	public void initialiseSignalState() {
		super.initialiseSignalState();
		for (String signalName: stateMap.keySet()) {
			SignalState signalState = stateMap.get(signalName);
			Node zeroNode = net.getNodeByReference(signalName + "_0");
			if (zeroNode instanceof Place) {
				Place zeroPlace = (Place)zeroNode;
				signalState.value = ((zeroPlace.getTokens() > 0) ? 0 : 1);
			}
			Node oneNode= net.getNodeByReference(signalName + "_1");
			if (oneNode instanceof Place) {
				Place onePlace = (Place)oneNode;
				signalState.value = ((onePlace.getTokens() > 0) ? 1 : 0);
			}
		}
	}

	// return first enabled transition
	public SignalTransition getContactExcitedTransition(VisualContact contact) {
		SignalTransition result = null;
		if ((converter != null) && converter.isDriver(contact)) {
			SignalStg signalStg = converter.getSignalStg(contact);
			for (VisualSignalTransition transition : signalStg.getAllVisualTransitions()) {
				if (net.isEnabled(transition.getReferencedTransition())) {
					result = transition.getReferencedTransition();
					break;
				}
			}
		}
		return result;
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(),
			new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {
					return node instanceof VisualContact;
				}
			});

		if (node != null) {
			SignalTransition st = getContactExcitedTransition((VisualContact) node);
			if (st != null) {
				executeTransition(e.getEditor(), st);
			}
		}
	}

	@Override
	protected boolean isContainerExcited(Container container) {
		if (excitedContainers.containsKey(container)) return excitedContainers.get(container);
		boolean ret = false;
		for (Node node: container.getChildren()) {
			if (node instanceof VisualContact) {
				SignalTransition transition = getContactExcitedTransition((VisualContact)node);
				ret=ret || (transition != null);
			}
			if (node instanceof Container) {
				ret = ret || isContainerExcited((Container)node);
			}
			if (ret) {
				break;
			}
		}
		excitedContainers.put(container, ret);
		return ret;
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if (converter == null) return null;
				if (node instanceof VisualContact) {
					VisualContact contact = (VisualContact)node;
					SignalStg signalStg = converter.getSignalStg(contact);
					if (signalStg != null) {
						Node transition = getTraceCurrentNode();
						if ((transition != null) && signalStg.containsDirectlyOrByReference(transition)) {
							return new Decoration() {
								@Override
								public Color getColorisation() {
									return CommonSimulationSettings.getEnabledBackgroundColor();
								}
								@Override
								public Color getBackground() {
									return CommonSimulationSettings.getEnabledForegroundColor();
								}
							};
						}
						final boolean isOne = (signalStg.P1.getReferencedPlace().getTokens() == 1);
						final boolean isZero = (signalStg.P0.getReferencedPlace().getTokens() == 1);
						final boolean isExcited = (getContactExcitedTransition(contact) != null);
						return new Decoration() {
							@Override
							public Color getColorisation() {
								if (isExcited) {
									return CommonSimulationSettings.getEnabledForegroundColor();
								}
								return null;
							}
							@Override
							public Color getBackground() {
								if (isExcited) {
									return CommonSimulationSettings.getEnabledBackgroundColor();
								} else {
									if (isOne && !isZero) {
										return CircuitSettings.getActiveWireColor();
									}
									if (!isOne && isZero) {
										return CircuitSettings.getInactiveWireColor();
									}
								}
								return null;
							}
						};
					}
				} else if ((node instanceof VisualJoint) || (node instanceof VisualCircuitConnection)) {
					SignalStg signalStg = converter.getSignalStg((VisualNode)node);
					if (signalStg != null) {
						final boolean isOne = (signalStg.P1.getReferencedPlace().getTokens() == 1);
						final boolean isZero = (signalStg.P0.getReferencedPlace().getTokens() == 1);
						return new Decoration() {
							@Override
							public Color getColorisation() {
								if (isOne && !isZero) {
									return CircuitSettings.getActiveWireColor();
								}
								if (!isOne && isZero) {
									return CircuitSettings.getInactiveWireColor();
								}
								return null;
							}
							@Override
							public Color getBackground() {
								return null;
							}
						};
					}
				} else if (node instanceof VisualPage || node instanceof VisualGroup) {
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
}
