package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.renderers.CElementRenderer;
import org.workcraft.plugins.circuit.renderers.CElementRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.renderers.GateRenderer;


@DisplayName("Function")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/icons/svg/circuit-formula.svg")
public class VisualFunctionComponent extends VisualCircuitComponent {
	private ComponentRenderingResult renderingResult = null;

	public VisualFunctionComponent(CircuitComponent component) {
		super(component);
	}

	private ComponentRenderingResult getRenderingResult() {
		if (renderingResult != null) {
			return renderingResult;
		}
		// Find a single gate output
		VisualFunctionContact gateOutput = null;
		for (Node n: getChildren()) {
			if (n instanceof VisualFunctionContact) {
				VisualFunctionContact vc = (VisualFunctionContact)n;
				if (vc.getIOType() == IOType.OUTPUT) {
					if (gateOutput == null) {
						gateOutput = vc;
					} else {
						// more than one output - not a gate
						gateOutput = null;
						break;
					}
				}
			}
		}
		// If gate output found render its visual representation according to the set and reset functions
		if (gateOutput != null) {
			switch (getRenderType()) {
			case BOX:
				renderingResult = null;
				break;
			case GATE:
				GateRenderer.foreground = getForegroundColor();
				GateRenderer.background = getFillColor();
				if (gateOutput.getSetFunction() == null) {
					renderingResult = null;
				} else if (gateOutput.getResetFunction() == null) {
					renderingResult = GateRenderer.renderGate(gateOutput.getSetFunction());
				} else {
					renderingResult = CElementRenderer.renderGate(gateOutput.getSetFunction(), gateOutput.getResetFunction());
				}
				break;
			default:
				break;
			}
		}
		return renderingResult;
	}

	public void resetRenderingResult() {
		renderingResult = null;
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		ComponentRenderingResult res = getRenderingResult();
		if (res == null) {
			return super.hitTestInLocalSpace(pointInLocalSpace);
		} else {
			return res.boundingBox().contains(pointInLocalSpace);
		}
	}

	@Override
	public void setRenderType(RenderType renderType) {
		super.setRenderType(renderType);
		resetRenderingResult();
		sendNotification(new PropertyChangedEvent(this, "render type"));
	}

	private boolean firstUpdate = true;

	@Override
	protected void updateStepPositions() {
		ComponentRenderingResult res = getRenderingResult();
		if (res == null) {
			super.updateStepPositions();
		} else if (firstUpdate) {
  		    // suppress notifications at first recalculation of contact coordinates
			firstUpdate = false;
		} else {
			AffineTransform at = new AffineTransform();
			AffineTransform bt = new AffineTransform();
			VisualContact v = getMainContact();
			if (v != null) {
				at = VisualContact.Direction.getDirectionTransform(v.getDirection());
			}
			for (Node n: this.getChildren()) {
				bt.setTransform(at);
				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact vc = (VisualFunctionContact)n;
					if (vc.getIOType() == IOType.OUTPUT) {
						bt.translate(snapP5(res.boundingBox().getMaxX() + GateRenderer.contactMargin), 0);
					}
					if (vc.getIOType() == IOType.INPUT) {
						String vcName = vc.getName();
						Point2D position = res.contactPositions().get(vcName);
						if (position != null) {
							bt.translate(snapP5(res.boundingBox().getMinX() - GateRenderer.contactMargin), position.getY());
						}
					} else {
						bt.translate(snapP5(res.boundingBox().getMaxX() + GateRenderer.contactMargin), 0);
					}
					// here we only need to change position, do not do the rotation
					AffineTransform ct = new AffineTransform();
					ct.translate(bt.getTranslateX(), bt.getTranslateY());
					vc.setTransform(ct);
				}
			}
		}
	}

	@Override
	public void notify(StateEvent e) {
		super.notify(e);
		if (e instanceof PropertyChangedEvent) {
			PropertyChangedEvent pc = (PropertyChangedEvent)e;
			if (pc.getPropertyName().equals("direction")) {
				if (getMainContact()==pc.getSender()&&getRenderingResult()!=null) {
					updateStepPositions();
				}
			}
		}
	}

	private void drawContactLines(Graphics2D g, ComponentRenderingResult rr, AffineTransform at) {
		g.setStroke(new BasicStroke((float)CircuitSettings.getWireWidth()));
		g.setColor(GateRenderer.foreground);
		for (Node node: this.getChildren()) {
			if (node instanceof VisualFunctionContact) {
				VisualFunctionContact vc = (VisualFunctionContact)node;
				Point2D pinPosition = null;
				if (vc.getIOType() == IOType.INPUT) {
					String cname = vc.getReferencedContact().getName();
					pinPosition = rr.contactPositions().get(cname);
				} else {
					pinPosition = new Point2D.Double(rr.boundingBox().getMaxX(), 0.0);
				}
				if (pinPosition != null) {
					Point2D p1 = at.transform(pinPosition, null);
					Point2D p2 = vc.getPosition();
					Line2D line = new Line2D.Double(p1, p2);
					g.draw(line);
				}
			}
		}
	}

	private void drawCelementSymbols(Graphics2D g, ComponentRenderingResult rr, AffineTransform at) {
		if (rr instanceof CElementRenderingResult) {
			CElementRenderingResult cr = (CElementRenderingResult)rr;
			Point2D labelPosition = cr.getLabelPosition();
			if (labelPosition != null) {
				at.transform(labelPosition, labelPosition);
				Arc2D cShape = new Arc2D.Double(labelPosition.getX()-0.2, labelPosition.getY()-0.2, 0.4, 0.4, 60, 240, Arc2D.OPEN);
				g.draw(cShape);
			}

			Point2D plusPosition = cr.getPlusPosition();
			if (plusPosition != null) {
				at.transform(plusPosition, plusPosition);
				Path2D plusShape = new Path2D.Double();
				plusShape.moveTo(plusPosition.getX()-0.15, plusPosition.getY());
				plusShape.lineTo(plusPosition.getX()+0.15, plusPosition.getY());
				plusShape.moveTo(plusPosition.getX(), plusPosition.getY()-0.15);
				plusShape.lineTo(plusPosition.getX(), plusPosition.getY()+0.15);
				g.draw(plusShape);
			}

			Point2D minusPosition = cr.getMinusPosition();
			if (minusPosition != null) {
				at.transform(minusPosition, minusPosition);
				Line2D minusShape = new Line2D.Double(minusPosition.getX()-0.15, minusPosition.getY(), minusPosition.getX()+0.15, minusPosition.getY());
				g.draw(minusShape);
			}
		}
	}

	@Override
	public void draw(DrawRequest r) {
		ComponentRenderingResult rr = getRenderingResult();
		if (rr == null) {
			super.draw(r);
		} else {
			Graphics2D g = r.getGraphics();
			cacheRenderedText(r); // needed to better estimate the bounding box

			// Determine rotation by the direction of the main contact (usually the only output)
			VisualContact contact = getMainContact();
			AffineTransform at = new AffineTransform();
			AffineTransform bt = new AffineTransform();
			if (contact != null) {
				switch (contact.getDirection()) {
				case NORTH:
					at.quadrantRotate(3);
					bt.quadrantRotate(1);
					break;
				case SOUTH:
					at.quadrantRotate(1);
					bt.quadrantRotate(3);
					break;
				case WEST:
					at.quadrantRotate(2);
					bt.quadrantRotate(2);
					break;
				case EAST:
					break;
				}
			}

			// Draw the component in its coordinates
			g.transform(at);
			if (!getIsEnvironment()) {
				g.setStroke(new BasicStroke((float)CircuitSettings.getBorderWidth()));
			} else {
				float dash[] = {0.05f, 0.05f};
				g.setStroke(new BasicStroke((float)CircuitSettings.getBorderWidth(),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
			}
			GateRenderer.foreground = Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation());
			GateRenderer.background  = Coloriser.colorise(getFillColor(), r.getDecoration().getBackground());
			rr.draw(g);
			g.transform(bt);

			drawContactLines(g, rr, at);
			drawCelementSymbols(g, rr, at);
			drawLabelInLocalSpace(r);
			drawNameInLocalSpace(r);
		}
	}

}
