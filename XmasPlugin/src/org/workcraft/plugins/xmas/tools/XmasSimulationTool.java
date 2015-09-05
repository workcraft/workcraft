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
import org.workcraft.plugins.xmas.components.QueueDecoration;
import org.workcraft.plugins.xmas.components.SlotState;
import org.workcraft.plugins.xmas.components.VisualQueueComponent;
import org.workcraft.plugins.xmas.components.VisualSinkComponent;
import org.workcraft.plugins.xmas.components.VisualSourceComponent;
import org.workcraft.plugins.xmas.components.VisualXmasConnection;
import org.workcraft.plugins.xmas.components.VisualXmasContact;
import org.workcraft.plugins.xmas.stg.QueueStg;
import org.workcraft.plugins.xmas.stg.SignalStg;
import org.workcraft.plugins.xmas.stg.SinkStg;
import org.workcraft.plugins.xmas.stg.SourceStg;
import org.workcraft.plugins.xmas.stg.StgGenerator;
import org.workcraft.util.Func;

public class XmasSimulationTool extends StgSimulationTool {
	private static final Color COLOR_IRDY = Color.RED;
	private static final Color COLOR_TRDY = Color.BLUE;
	private static final Color COLOR_BOTH_READY = Color.MAGENTA;
	private static final Color COLOR_NONE_READY = Color.BLACK;
	private static final Color COLOR_CONTACT_IRDY = COLOR_IRDY;
	private static final Color COLOR_CONTACT_TRDY = COLOR_TRDY;
	private static final Color COLOR_CONTACT_NOT_READY = Color.WHITE;
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
					return node instanceof VisualTransformableNode;
				}
			});

		Transition transition = null;
		if (node instanceof VisualTransformableNode) {
			AffineTransform rootToLocalTransform = TransformHelper.getTransform(e.getModel().getRoot(), node);
			Point2D posLocal = rootToLocalTransform.transform(posRoot, null);
			Point2D posNode = ((VisualTransformableNode)node).getParentToLocalTransform().transform(posLocal, null);
			if (node instanceof VisualXmasContact) {
				SignalStg contactStg = generator.getContactStg((VisualXmasContact)node);
				transition = getExcitedTransition(contactStg.getAllTransitions());
			} else if (node instanceof VisualSourceComponent) {
				SourceStg sourceStg = generator.getSourceStg((VisualSourceComponent)node);
				transition = getExcitedTransition(sourceStg.oracle.getAllTransitions());
			} else if (node instanceof VisualSinkComponent) {
				SinkStg sinkStg = generator.getSinkStg((VisualSinkComponent)node);
				transition = getExcitedTransition(sinkStg.oracle.getAllTransitions());
			} else if (node instanceof VisualQueueComponent) {
				QueueStg queueStg = generator.getQueueStg((VisualQueueComponent)node);
				if (posNode.getY() < -0.3 ) {
					transition = getExcitedTransition(queueStg.getHeadTransitions());
				} else if (posNode.getY() > +0.3){
					transition = getExcitedTransition(queueStg.getTailTransitions());
				} else {
					transition = getExcitedTransition(queueStg.getMemTransitions());
				}
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
					final SignalStg contactStg = generator.getContactStg(contact);
					final boolean isExcited = (getExcitedTransition(contactStg.getAllTransitions()) != null);
					final boolean isInTrace = generator.isRelated(node, traceCurrentNode);
					final boolean isReady = (contactStg.zero.getReferencedPlace().getTokens() == 0);

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
										return COLOR_CONTACT_TRDY;
									} else {
										return COLOR_CONTACT_IRDY;
									}
								}
								return COLOR_CONTACT_NOT_READY;
							}
						}
					};
				} else if (node instanceof VisualXmasConnection) {
					final VisualXmasConnection connection = (VisualXmasConnection)node;
					final VisualXmasContact firstContact = (VisualXmasContact)connection.getFirst();
					final VisualXmasContact secondContact = (VisualXmasContact)connection.getSecond();
					final SignalStg firstStg = generator.getContactStg(firstContact);
					final SignalStg secondStg = generator.getContactStg(secondContact);
					final boolean firstReady = (firstStg.zero.getReferencedPlace().getTokens() == 0);
					final boolean secondReady = (secondStg.zero.getReferencedPlace().getTokens() == 0);

					return new Decoration() {
						@Override
						public Color getColorisation() {
							if (firstReady && secondReady) {
								return COLOR_BOTH_READY;
							} if (firstReady) {
								return COLOR_IRDY;
							} if (secondReady) {
								return COLOR_TRDY;
							}
							return COLOR_NONE_READY;
						}

						@Override
						public Color getBackground() {
							return null;
						}
					};
				} else if (node instanceof VisualSourceComponent) {
					final VisualSourceComponent source = (VisualSourceComponent)node;
					final SourceStg sourceStg = generator.getSourceStg(source);
					final boolean isExcited = (getExcitedTransition(sourceStg.oracle.getAllTransitions()) != null);
					final boolean isInTrace = generator.isRelated(node, traceCurrentNode);

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
							}
							return null;
						}
					};
				} else if (node instanceof VisualSinkComponent) {
					final VisualSinkComponent sink = (VisualSinkComponent)node;
					final SinkStg sinkStg = generator.getSinkStg(sink);
					final boolean isExcited = (getExcitedTransition(sinkStg.oracle.getAllTransitions()) != null);
					final boolean isInTrace = generator.isRelated(node, traceCurrentNode);

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
							}
							return null;
						}
					};
				} else if (node instanceof VisualQueueComponent) {
					final VisualQueueComponent queue = (VisualQueueComponent)node;
					final QueueStg queueStg = generator.getQueueStg(queue);
					final boolean isInTrace = generator.isRelated(node, traceCurrentNode);

					return new QueueDecoration() {
						@Override
						public SlotState getSlotState(int i) {
							SlotState result = null;
							int capacity = queue.getReferencedQueueComponent().getCapacity();
							if ((i >= 0) && (i < capacity)) {
								boolean isFull = (queueStg.memList.get(i).one.getReferencedPlace().getTokens() != 0);
								boolean isHead = (queueStg.headList.get(i).one.getReferencedPlace().getTokens() != 0);
								boolean isTail= (queueStg.tailList.get(i).one.getReferencedPlace().getTokens() != 0);
								boolean isMemExcited = (getExcitedTransition(queueStg.memList.get(i).getAllTransitions()) != null);
								boolean isHeadExcited = (getExcitedTransition(queueStg.headList.get(i).getAllTransitions()) != null);
								boolean isTailExcited = (getExcitedTransition(queueStg.tailList.get(i).getAllTransitions()) != null);
								result = new SlotState(isFull, isHead, isTail, isMemExcited, isHeadExcited, isTailExcited);
							}
							return result;
						}

						@Override
						public Color getColorisation() {
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
