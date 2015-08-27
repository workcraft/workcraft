package org.workcraft.plugins.xmas.tools;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonSimulationSettings;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.tools.StgSimulationTool;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.components.VisualXmasConnection;
import org.workcraft.plugins.xmas.components.VisualXmasContact;
import org.workcraft.plugins.xmas.stg.ContactStg;
import org.workcraft.plugins.xmas.stg.StgGenerator;
import org.workcraft.util.Func;

public class XmasSimulationTool extends StgSimulationTool {
	private StgGenerator generator;

	@Override
	public void activated(GraphEditor editor) {
		super.activated(editor);
		setStatePaneVisibility(false);
	}

	@Override
	public void deactivated(GraphEditor editor) {
		super.deactivated(editor);
		this.generator = null;
	}

	@Override
	public VisualModel getUnderlyingModel(VisualModel model) {
		generator = new StgGenerator((VisualXmas)model);
		return generator.getStg();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Point2D posRoot = e.getPosition();
		Node node = HitMan.hitDeepest(posRoot, e.getModel().getRoot(),
			new Func<Node, Boolean>() {
				@Override
				public Boolean eval(Node node) {
					return node instanceof VisualXmasContact;
				}
			});

		Transition transition = null;
		if (node instanceof VisualTransformableNode) {
			AffineTransform rootToLocalTransform = TransformHelper.getTransform(e.getModel().getRoot(), node);
			Point2D posLocal = rootToLocalTransform.transform(posRoot, null);
			Point2D posNode = ((VisualTransformableNode)node).getParentToLocalTransform().transform(posLocal, null);
			if (node instanceof VisualXmasContact) {
				ContactStg contactStg = generator.getContactStg((VisualXmasContact)node);
				transition = getExcitedTransition(contactStg.getAllTransitions());
			}
		}

		if (transition != null) {
			executeTransition(e.getEditor(), transition);
		}
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {
			@Override
			public Decoration getDecoration(Node node) {
				final Node traceCurrentNode = getTraceCurrentNode();
				if (node instanceof VisualXmasContact) {
					final VisualXmasContact contact = (VisualXmasContact)node;
					final ContactStg contactStg = generator.getContactStg(contact);
					final boolean isExcited = (getExcitedTransition(contactStg.getAllTransitions()) != null);
					final boolean isInTrace = generator.isRelated(node, traceCurrentNode);
					final boolean isReady = (contactStg.rdy0.getReferencedPlace().getTokens() == 0);

					return new Decoration() {
						@Override
						public Color getColorisation() {
							if (isExcited) {
								if (isInTrace) {
									return CommonSimulationSettings.getEnabledBackgroundColor();
								} else {
									return CommonSimulationSettings.getEnabledForegroundColor();
								}
							}
							return null;
						}

						@Override
						public Color getBackground() {
							if (isExcited) {
								if (isInTrace) {
									return CommonSimulationSettings.getEnabledForegroundColor();
								} else {
									return CommonSimulationSettings.getEnabledBackgroundColor();
								}
							} else {
								if (isReady) {
									if (contact.isInput()) {
										return Color.GREEN;
									} else {
										return Color.BLUE;
									}
								} else {
									return Color.WHITE;
								}
							}
						}
					};
				} else if (node instanceof VisualXmasConnection) {
					final VisualXmasConnection connection = (VisualXmasConnection)node;
					final VisualXmasContact firstContact = (VisualXmasContact)connection.getFirst();
					final VisualXmasContact secondContact = (VisualXmasContact)connection.getSecond();
					final ContactStg firstStg = generator.getContactStg(firstContact);
					final ContactStg secondStg = generator.getContactStg(secondContact);
					final boolean firstReady = (firstStg.rdy0.getReferencedPlace().getTokens() == 0);
					final boolean secondReady = (secondStg.rdy0.getReferencedPlace().getTokens() == 0);

					return new Decoration() {
						@Override
						public Color getColorisation() {
							if (firstReady && secondReady) {
								return Color.RED;
							} if (firstReady) {
								return Color.BLUE;
							} if (secondReady) {
								return Color.GREEN;
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

	private Transition getExcitedTransition(Collection<VisualSignalTransition> ts) {
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
