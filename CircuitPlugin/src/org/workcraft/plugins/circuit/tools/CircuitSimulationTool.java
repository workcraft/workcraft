package org.workcraft.plugins.circuit.tools;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.tools.STGSimulationTool;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class CircuitSimulationTool extends STGSimulationTool {
	JButton copyInitButton;

	@Override
	public VisualModel getUnderlyingModel(VisualModel model) {
		return STGGenerator.generate((VisualCircuit)model);
	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);
		copyInitButton = new JButton("Copy init");
		copyInitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (VisualContact vc : Hierarchy.getDescendantsOfType(editor.getModel().getRoot(), VisualContact.class)) {
					Contact c = (Contact) vc.getReferencedComponent();
					if (!vc.getReferencedTransitions().isEmpty()) {
						c.setInitOne(vc.getReferencedOnePlace().getTokens() == 1);
					}
				}
			}
		});
		controlPanel.add(copyInitButton);
	}

	// return first enabled transition
	public SignalTransition isContactExcited(VisualContact c) {
		boolean up = false;
		boolean down = false;

		SignalTransition st = null;
		if (c == null)
			return null;

		for (SignalTransition tr : c.getReferencedTransitions()) {
			if (net.isEnabled(tr)) {
				if (st == null)
					st = tr;
				if (tr.getDirection() == Direction.MINUS)
					down = true;
				if (tr.getDirection() == Direction.PLUS)
					up = true;
				if (up && down)
					break;
			}
		}

		if (up && down)
			return null;
		return st;
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

		if (node == null)
			return;
		SignalTransition st = isContactExcited((VisualContact) node);
		if (st != null) {
			executeTransition(e.getEditor(), st);
		}
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				if (node instanceof VisualContact) {
					VisualContact contact = (VisualContact) node;
					String transitionId = null;
					Node transition2 = null;
					if (branchTrace.canProgress()) {
						transitionId = branchTrace.getCurrent();
						transition2 = net.getNodeByReference(transitionId);
					} else if (branchTrace.isEmpty() && mainTrace.canProgress()) {
						transitionId = mainTrace.getCurrent();
						transition2 = net.getNodeByReference(transitionId);
					}

					if (contact.getReferencedTransitions().contains(transition2)) {
						return new Decoration() {
							@Override
							public Color getColorisation() {
								return CommonVisualSettings.getEnabledBackgroundColor();
							}
							@Override
							public Color getBackground() {
								return CommonVisualSettings.getEnabledForegroundColor();
							}
						};
					}
					if ((contact.getReferencedOnePlace() == null) || (contact.getReferencedZeroPlace() == null)) {
						return null;
					}
					final boolean isOne = contact.getReferencedOnePlace().getTokens() == 1;
					final boolean isZero = contact.getReferencedZeroPlace().getTokens() == 1;
					final boolean isExcited = (isContactExcited(contact) != null);
					return new Decoration() {
						@Override
						public Color getColorisation() {
							if (isExcited) {
								return CommonVisualSettings.getEnabledForegroundColor();
							}
							return null;
						}
						@Override
						public Color getBackground() {
							if (isExcited) {
								return CommonVisualSettings.getEnabledBackgroundColor();
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
				} else if (node instanceof VisualJoint) {
					VisualJoint vj = (VisualJoint) node;
					if (vj.getReferencedOnePlace() == null || vj.getReferencedZeroPlace() == null) {
						return null;
					}
					final boolean isOne = vj.getReferencedOnePlace().getTokens() == 1;
					final boolean isZero = vj.getReferencedZeroPlace().getTokens() == 1;
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
				} else if (node instanceof VisualCircuitConnection) {
					VisualCircuitConnection vc = (VisualCircuitConnection) node;
					if (vc.getReferencedOnePlace() == null	|| vc.getReferencedZeroPlace() == null) {
						return null;
					}
					final boolean isOne = vc.getReferencedOnePlace().getTokens() == 1;
					final boolean isZero = vc.getReferencedZeroPlace().getTokens() == 1;
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
				return null;
			}
		};
	}
}
