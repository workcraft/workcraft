package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
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
import org.workcraft.plugins.circuit.renderers.GateRenderer;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;


@DisplayName("Function")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/icons/svg/circuit-formula.svg")

public class VisualFunctionComponent extends VisualCircuitComponent {

	ComponentRenderingResult renderingResult = null;

	private ComponentRenderingResult getRenderingResult() {

		if (getRenderType()==RenderType.BOX) return null;

		if (getChildren().isEmpty()) return null;

		if (renderingResult==null) {


			// derive picture from the first output contact available
			for (Node n: getChildren()) {
				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact vc = (VisualFunctionContact)n;

					if (vc.getFunction().getSetFunction()==null) return null;

					if (vc.getIOType()==IOType.OUTPUT) {
						switch (getRenderType()) {
						case GATE:
							renderingResult = GateRenderer.renderGate(vc.getFunction().getSetFunction());
							break;
						case BUFFER:
							renderingResult = BufferRenderer.renderGate(vc.getFunction().getSetFunction());
							break;
						case C_ELEMENT:
							if (vc.getFunction().getResetFunction()!=null) {
								renderingResult = CElementRenderer.renderGate(
										vc.getFunction().getSetFunction(), vc.getFunction().getResetFunction());
							} else {
								return null;
							}
							break;
						default:
								break;
						}
					}
				}
			}

			if (renderingResult!=null) {
				updateStepPositions();
				updateTotalBB();
			}
		}
		return renderingResult;
	}


	public void resetRenderingResult() {
		renderingResult = null;
	}

	public VisualFunctionComponent(CircuitComponent component) {
		super(component);

		if (component.getChildren().isEmpty()) {
			this.addFunction("x", null, false);
		}

	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (getRenderingResult()!=null) {
			return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
		} else {
			return super.hitTestInLocalSpace(pointInLocalSpace);
		}
	}


	public VisualFunctionContact addFunction(String name, IOType ioType, boolean allowShort) {
		name = Contact.getNewName(this.getReferencedComponent(), name, null, allowShort);

		VisualContact.Direction dir=null;
		if (ioType==null) ioType = IOType.OUTPUT;

		dir=VisualContact.Direction.WEST;
		if (ioType==IOType.OUTPUT)
			dir=VisualContact.Direction.EAST;

		FunctionContact c = new FunctionContact(ioType);

		VisualFunctionContact vc = new VisualFunctionContact(c, dir, name);

		addContact(vc);

		return vc;
	}

	public VisualFunctionContact getOrCreateInput(String arg) {

		for (VisualFunctionContact c : Hierarchy.filterNodesByType(getChildren(), VisualFunctionContact.class)) {
			if(c.getName().equals(arg)) return c;
		}

		VisualFunctionContact vc = addFunction(arg, IOType.INPUT, true);

		vc.setSetFunction(One.instance());
		vc.setResetFunction(One.instance());

		return vc;
	}

	@Override
	public void setRenderType(RenderType renderType) {
		super.setRenderType(renderType);
		resetRenderingResult();
	}


	private Rectangle2D getResBB() {

		ComponentRenderingResult res = getRenderingResult();

		if (res==null) return null;
		Rectangle2D rec = new Rectangle2D.Double();

		rec.setRect(getRenderingResult().boundingBox());

		Point2D p1 = new Point2D.Double(rec.getMinX(), rec.getMinY());
		Point2D p2 = new Point2D.Double(rec.getMaxX(), rec.getMaxY());

		AffineTransform at = VisualContact.getDirectionTransform(getMainContact().getDirection());

		at.transform(p1, p1);
		at.transform(p2, p2);

		double x1 = Math.min(p1.getX(), p2.getX());
		double y1 = Math.min(p1.getY(), p2.getY());
		double x2 = Math.max(p1.getX(), p2.getX());
		double y2 = Math.max(p1.getY(), p2.getY());

		rec.setRect(x1, y1, x2-x1, y2-y1);

		return rec;
	}

	@Override
	protected void updateTotalBB() {

		ComponentRenderingResult res = getRenderingResult();

		if (res!=null) {

			Rectangle2D rec = getResBB();

			totalBB = BoundingBoxHelper.mergeBoundingBoxes(Hierarchy.getChildrenOfType(this, Touchable.class));
			totalBB = BoundingBoxHelper.union(rec, totalBB);
		} else {
			super.updateTotalBB();
		}
	}

	@Override
	protected void updateStepPositions()
	{
		ComponentRenderingResult res = getRenderingResult();
		if (res!=null)
		{
			VisualContact v = getMainContact();

			AffineTransform at = new AffineTransform();
			AffineTransform bt = new AffineTransform();

			if (v!=null) at = VisualContact.getDirectionTransform(v.getDirection());

			for (Node n: this.getChildren()) {

				bt.setTransform(at);

				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact vc = (VisualFunctionContact)n;

					if (vc.getIOType() == IOType.OUTPUT) {
						bt.translate(
							snapP5(res.boundingBox().getMaxX() + GateRenderer.contactMargin),0);

						vc.setTransform(bt);
						continue;
					}

					if (vc.getIOType() != IOType.INPUT) continue;

					Point2D position = res.contactPositions().get(vc.getName());

					if (position != null)
					{
						bt.translate(
								snapP5(res.boundingBox().getMinX() - GateRenderer.contactMargin),
								position.getY());

						vc.setTransform(bt);
					}

				}
			}
		}
		else
		{
			super.updateStepPositions();
		}
	}

	@Override
	public void notify(StateEvent e) {
		super.notify(e);
		if (e instanceof PropertyChangedEvent) {

			PropertyChangedEvent pc = (PropertyChangedEvent)e;


			if (pc.getPropertyName().equals("direction")) {
				if (getMainContact()==pc.getSender()&&getRenderingResult()!=null)
					updateStepPositions();
			}
		}
	}

	@Override
	public void draw(DrawRequest r) {

		ComponentRenderingResult res = getRenderingResult();
		Graphics2D g = r.getGraphics();

		Color colorisation = r.getDecoration().getColorisation();
		Color col1 = Coloriser.colorise(CommonVisualSettings.getForegroundColor(), colorisation);
		Color col2 = Coloriser.colorise(CommonVisualSettings.getBackgroundColor(), colorisation);


		if (res!=null) {

			if (!getIsEnvironment()) {
				g.setStroke(new BasicStroke((float)CircuitSettings.getComponentBorderWidth()));
			} else {
				float dash[] = {0.05f, 0.05f};

				g.setStroke(
						new BasicStroke(
							(float)CircuitSettings.getComponentBorderWidth(),
							BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f,
							dash, 0.0f)
							);
			}


			Point2D mp=null, lp=null, pp=null;

			// draw component label
			updateNameGlyph(r.getGraphics());

			r.getGraphics().setColor(Coloriser.colorise(CommonVisualSettings.getForegroundColor(), r.getDecoration().getColorisation()));

			r.getGraphics().setFont(nameFont);
			Rectangle2D rec = getResBB();

			r.getGraphics().drawString(getName(), (float)(rec.getMaxX()-0.2),
					(float)(rec.getMaxY()+0.5));

			// draw the rest
			GateRenderer.foreground = col1;
			GateRenderer.background = col2;


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
			g.setStroke(new BasicStroke((float)CircuitSettings.getCircuitWireWidth()));
			g.setColor(col1);

			Line2D line;


			// draw output and input lines

			for (Node n: this.getChildren()) {
				if (n instanceof VisualFunctionContact) {
					VisualFunctionContact vc = (VisualFunctionContact)n;

					if (vc.getIOType() == IOType.OUTPUT) {
						line = new Line2D.Double(
								res.boundingBox().getMaxX(), 0,
								snapP5(res.boundingBox().getMaxX()+GateRenderer.contactMargin), 0);
						g.draw(line);
						continue;
					}

					if (vc.getIOType() != IOType.INPUT) continue;

					Point2D position = res.contactPositions().get(vc.getName());

					if (position != null)
					{
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
//				Line2D l = new Line2D.Double(lp.getX(),lp.getY(),lp.getX()+0.01,lp.getY()+0.01);
//				g.draw(l);

				r.getGraphics().drawString("C",(float)lp.getX()-(float)0.2,
							(float)lp.getY()+(float)0.2);
			}

			if (pp!=null) {
//				Line2D l = new Line2D.Double(pp.getX(),pp.getY(),pp.getX()+0.01,pp.getY()+0.01);
//				g.draw(l);
				r.getGraphics().drawString("+",(float)pp.getX()-(float)0.15,
				(float)pp.getY()+(float)0.15);
			}


			if (mp!=null) {
//				Line2D l = new Line2D.Double(mp.getX(),mp.getY(),mp.getX()+0.01,mp.getY()+0.01);
//				g.draw(l);
				r.getGraphics().drawString("-",(float)mp.getX()-(float)0.15,
				(float)mp.getY()+(float)0.15);
			}



		} else {
			super.draw(r);
		}

	}

}
