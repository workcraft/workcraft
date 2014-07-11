package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.renderers.BufferRenderer;
import org.workcraft.plugins.circuit.renderers.CElementRenderer;
import org.workcraft.plugins.circuit.renderers.CElementRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.renderers.GateRenderer;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.util.Hierarchy;


@DisplayName("Function")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/icons/svg/circuit-formula.svg")
public class VisualFunctionComponent extends VisualCircuitComponent {

	public VisualFunctionComponent(CircuitComponent component) {
		super(component);
//		if (component.getChildren().isEmpty()) {
//			this.addFunction("x", null, false);
//		}
	}

	ComponentRenderingResult renderingResult = null;

	private ComponentRenderingResult getRenderingResult() {
		if (getRenderType()==RenderType.BOX) return null;
		if (getChildren().isEmpty()) return null;
		if (renderingResult==null) {
			// derive picture from the first output contact available
			for (Node n: getChildren()) {
				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact vc = (VisualFunctionContact)n;
					if (vc.getSetFunction()==null) {
						return null;
					}
					if (vc.getIOType()==IOType.OUTPUT) {
						switch (getRenderType()) {
						case GATE:
							GateRenderer.foreground = getForegroundColor();
							GateRenderer.background = getFillColor();
							renderingResult = GateRenderer.renderGate(vc.getSetFunction());
							break;
						case BUFFER:
							renderingResult = BufferRenderer.renderGate(vc.getSetFunction());
							break;
						case C_ELEMENT:
							if (vc.getResetFunction()!=null) {
								renderingResult = CElementRenderer.renderGate(
									vc.getSetFunction(), vc.getResetFunction());
							}
							break;
						default:
							break;
						}
					}
				}
			}

			if (renderingResult != null) {
				updateStepPositions();
				updateTotalBB();
			}
		}
		return renderingResult;
	}

	public void resetRenderingResult() {
		renderingResult = null;
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (getRenderingResult()!=null) {
			return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
		} else {
			return super.hitTestInLocalSpace(pointInLocalSpace);
		}
	}

	@Override
	public void setRenderType(RenderType renderType) {
		super.setRenderType(renderType);
		resetRenderingResult();
	}

	@Override
	protected void updateTotalBB() {
		ComponentRenderingResult res = getRenderingResult();
		if (res == null) {
			super.updateTotalBB();
		} else {
            Rectangle2D bb = new Rectangle2D.Double();
			bb.setRect(res.boundingBox());
			Point2D p1 = new Point2D.Double(bb.getMinX(), bb.getMinY());
			Point2D p2 = new Point2D.Double(bb.getMaxX(), bb.getMaxY());
			AffineTransform at = VisualContact.Direction.getDirectionTransform(getMainContact().getDirection());
			at.transform(p1, p1);
			at.transform(p2, p2);
			double x1 = Math.min(p1.getX(), p2.getX());
			double y1 = Math.min(p1.getY(), p2.getY());
			double x2 = Math.max(p1.getX(), p2.getX());
			double y2 = Math.max(p1.getY(), p2.getY());
			bb.setRect(x1, y1, x2-x1, y2-y1);

			totalBB = BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(this, Touchable.class));
			totalBB = BoundingBoxHelper.union(bb, totalBB);
		}
	}

	protected boolean firstUpdate = true;

	@Override
	protected void updateStepPositions() {
		ComponentRenderingResult res = getRenderingResult();
		if (res == null) {
			super.updateStepPositions();
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

						// here we only need to change position, do not do the rotation
						AffineTransform ct = new AffineTransform();
						ct.translate(bt.getTranslateX(), bt.getTranslateY());

						vc.setTransform(ct, !firstUpdate); // suppress notifications at first recalculation of contact coordinates
						continue;
					}

					if (vc.getIOType() != IOType.INPUT) continue;

					String vcName = vc.getName();
					Point2D position = res.contactPositions().get(vcName);
					if (position != null) {
						bt.translate(snapP5(res.boundingBox().getMinX() - GateRenderer.contactMargin), position.getY());

						// here we only need to change position, do not do the rotation
						AffineTransform ct = new AffineTransform();
						ct.translate(bt.getTranslateX(), bt.getTranslateY());

						vc.setTransform(ct, !firstUpdate); // suppress notifications at first recalculation of contact coordinates
					}
				}
			}
			firstUpdate = false;
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

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		GateRenderer.foreground = Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation());
		GateRenderer.background  = Coloriser.colorise(getFillColor(), r.getDecoration().getBackground());
		ComponentRenderingResult res = getRenderingResult();
		VisualCircuit vcircuit = (VisualCircuit)r.getModel();


		if (res == null) {
			super.draw(r);
		} else {
			drawLabelInLocalSpace(r);
			if (!getIsEnvironment()) {
				g.setStroke(new BasicStroke((float)CircuitSettings.getBorderWidth()));
			} else {
				float dash[] = {0.05f, 0.05f};
				g.setStroke(new BasicStroke((float)CircuitSettings.getBorderWidth(),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));
			}
			Point2D mp=null, lp=null, pp=null;

			// draw the rest
			VisualContact v = getMainContact();
			AffineTransform at = new AffineTransform();
			AffineTransform bt = new AffineTransform();

			if (res instanceof CElementRenderingResult) {
				CElementRenderingResult cr = (CElementRenderingResult)res;
				lp = cr.getLabelPosition();
				pp = cr.getPlusPosition();
				mp = cr.getMinusPosition();
			}

			if (v!=null) {
				switch (v.getDirection()) {
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
				g.transform(at);

				if (lp!=null) at.transform(lp, lp);
				if (mp!=null) at.transform(mp, mp);
				if (pp!=null) at.transform(pp, pp);
			}
			res.draw(g);

			// draw contact wires
			Stroke s = g.getStroke();
			g.setStroke(new BasicStroke((float)CircuitSettings.getWireWidth()));
			g.setColor(GateRenderer.foreground);
			Line2D line;

			// draw output and input lines
			for (Node n: this.getChildren()) {
				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact vc = (VisualFunctionContact)n;
					if (vc.getIOType() == IOType.OUTPUT) {
						line = new Line2D.Double(res.boundingBox().getMaxX(), 0,
							snapP5(res.boundingBox().getMaxX()+GateRenderer.contactMargin), 0);
						g.draw(line);
						continue;
					}

					if (vc.getIOType() != IOType.INPUT) continue;

					String cname = vc.getReferencedContact().getName();
					Point2D position = res.contactPositions().get(cname);
					if (position != null) {
						line = new Line2D.Double(snapP5(res.boundingBox().getMinX() - GateRenderer.contactMargin),
							position.getY(), position.getX(), position.getY());
						g.draw(line);
					}
				}
			}
			g.transform(bt);
			g.setStroke(s);

			// for C element draw letter C
			if (lp!=null) {
				Arc2D cShape = new Arc2D.Double(lp.getX()-0.2, lp.getY()-0.2, 0.4, 0.4, 60, 240, Arc2D.OPEN);
				g.draw(cShape);
			}
			if (pp!=null) {
				Path2D plusShape = new Path2D.Double();
				plusShape.moveTo(pp.getX()-0.15, pp.getY());
				plusShape.lineTo(pp.getX()+0.15, pp.getY());
				plusShape.moveTo(pp.getX(), pp.getY()-0.15);
				plusShape.lineTo(pp.getX(), pp.getY()+0.15);
				g.draw(plusShape);
			}
			if (mp!=null) {
				Line2D minusShape = new Line2D.Double(mp.getX()-0.15, mp.getY(), mp.getX()+0.15, mp.getY());
				g.draw(minusShape);
			}
		}
	}

}
