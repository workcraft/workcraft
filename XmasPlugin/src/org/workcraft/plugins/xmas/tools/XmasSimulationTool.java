package org.workcraft.plugins.xmas.tools;

import java.awt.Color;
import java.awt.event.MouseEvent;
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
import org.workcraft.plugins.xmas.components.VisualSwitchComponent;
import org.workcraft.plugins.xmas.components.VisualXmasConnection;
import org.workcraft.plugins.xmas.components.VisualXmasContact;
import org.workcraft.plugins.xmas.stg.ContactStg;
import org.workcraft.plugins.xmas.stg.QueueStg;
import org.workcraft.plugins.xmas.stg.SinkStg;
import org.workcraft.plugins.xmas.stg.SlotStg;
import org.workcraft.plugins.xmas.stg.SourceStg;
import org.workcraft.plugins.xmas.stg.StgGenerator;
import org.workcraft.plugins.xmas.stg.SwitchStg;
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
	public String getHintMessage() {
		String msg = "Click on the highlighted elements to activate it.";
		if (getExcitedTransition(generator.getClockStg().fallList) != null) {
			msg += " Right-click for the next clock tick.";
		}
		return msg;
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Transition transition = null;
		if (e.getButton() == MouseEvent.BUTTON3) {
			transition = getExcitedTransition(generator.getClockStg().fallList);
		}
		if (e.getButton() == MouseEvent.BUTTON1) {
			Point2D posRoot = e.getPosition();
			Node node = HitMan.hitDeepest(posRoot, e.getModel().getRoot(),
				new Func<Node, Boolean>() {
					@Override
					public Boolean eval(Node node) {
						return node instanceof VisualTransformableNode;
					}
				});

			if (node instanceof VisualTransformableNode) {
				AffineTransform rootToLocalTransform = TransformHelper.getTransform(e.getModel().getRoot(), node);
				Point2D posLocal = rootToLocalTransform.transform(posRoot, null);
				Point2D posNode = ((VisualTransformableNode)node).getParentToLocalTransform().transform(posLocal, null);
				transition = getClickedComponentTransition(node, posNode);
			}
		}
		if (transition != null) {
			executeTransition(e.getEditor(), transition);
			Collection<VisualSignalTransition> doneTransitions = generator.getDoneTransitions();
			doneTransitions.addAll(generator.getClockStg().riseList);
			Transition t = null;
			while ((t = getExcitedTransition(doneTransitions)) != null) {
				executeTransition(e.getEditor(), t);
			}
		}
	}

	private Transition getClickedComponentTransition(Node node, Point2D posNode) {
		Transition result = null;
		if (node instanceof VisualXmasContact) {
			ContactStg contactStg = generator.getContactStg((VisualXmasContact)node);
			result = getExcitedTransition(contactStg.rdy.getAllTransitions());
		} else if (node instanceof VisualSourceComponent) {
			SourceStg sourceStg = generator.getSourceStg((VisualSourceComponent)node);
			result = getExcitedTransition(sourceStg.oracle.getAllTransitions());
		} else if (node instanceof VisualSinkComponent) {
			SinkStg sinkStg = generator.getSinkStg((VisualSinkComponent)node);
			result = getExcitedTransition(sinkStg.oracle.getAllTransitions());
		} else if (node instanceof VisualSwitchComponent) {
			SwitchStg switchStg = generator.getSwitchStg((VisualSwitchComponent)node);
			result = getExcitedTransition(switchStg.oracle.getAllTransitions());
		} else if (node instanceof VisualQueueComponent) {
			VisualQueueComponent queue = (VisualQueueComponent)node;
			QueueStg queueStg = generator.getQueueStg(queue);
			int capacity = queue.getReferencedQueueComponent().getCapacity();
			int idx =  (int)Math.floor(0.5 * capacity  + posNode.getX() * queue.slotWidth);
			if (idx >= capacity) idx = capacity - 1;
			if (idx < 0) idx = 0;
			SlotStg slot = queueStg.slotList.get(idx);
			double headThreshold = 0.5 * queue.slotHeight - queue.headSize;
			double tailThreshold = 0.5 * queue.slotHeight - queue.tailSize;
			if (posNode.getY() < -headThreshold) {
				result = getExcitedTransition(slot.hd.rdy.getAllTransitions());
			} else if (posNode.getY() > tailThreshold){
				result = getExcitedTransition(slot.tl.rdy.getAllTransitions());
			} else {
				result = getExcitedTransition(slot.mem.getAllTransitions());
			}
		}
		return result;
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
					final boolean isReady = (contactStg.rdy.zero.getReferencedPlace().getTokens() == 0);

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
					final ContactStg firstStg = generator.getContactStg(firstContact);
					final ContactStg secondStg = generator.getContactStg(secondContact);
					final boolean firstReady = (firstStg.rdy.zero.getReferencedPlace().getTokens() == 0);
					final boolean secondReady = (secondStg.rdy.zero.getReferencedPlace().getTokens() == 0);

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
					final boolean isActive = (sourceStg.oracle.one.getReferencedPlace().getTokens() != 0);

					return new Decoration() {
						@Override
						public Color getColorisation() {
							if (isActive) {
								return COLOR_IRDY;
							}
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
					final boolean isActive = (sinkStg.oracle.one.getReferencedPlace().getTokens() != 0);

					return new Decoration() {
						@Override
						public Color getColorisation() {
							if (isActive) {
								return COLOR_TRDY;
							}
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
								SlotStg slot = queueStg.slotList.get(i);
								boolean isFull = (slot.mem.one.getReferencedPlace().getTokens() != 0);
								boolean isHead = (slot.hd.rdy.one.getReferencedPlace().getTokens() != 0);
								boolean isTail= (slot.tl.rdy.one.getReferencedPlace().getTokens() != 0);
								boolean isMemExcited = (getExcitedTransition(slot.mem.getAllTransitions()) != null);
								boolean isHeadExcited = (getExcitedTransition(slot.hd.getAllTransitions()) != null);
								boolean isTailExcited = (getExcitedTransition(slot.tl.getAllTransitions()) != null);
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
