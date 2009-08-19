package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualCircuitComponent extends VisualComponent {

	private HashSet<VisualContact> inputs = new HashSet<VisualContact>();
	private HashSet<VisualContact> outputs = new HashSet<VisualContact>();

	public VisualCircuitComponent(CircuitComponent component) {
		super(component);
		// testing...
		inputs.add(new VisualContact(component.addInput("A")));
		inputs.add(new VisualContact(component.addInput("B")));
		inputs.add(new VisualContact(component.addInput("C")));
		outputs.add(new VisualContact(component.addOutput("X")));
		outputs.add(new VisualContact(component.addOutput("Y")));
		outputs.add(new VisualContact(component.addOutput("Z")));

	}

	public VisualCircuitComponent(CircuitComponent component, Element xmlElement) {
		super(component, xmlElement);
	}


	protected Rectangle2D getContactLabelBB(Graphics2D g) {
		double maxi, maxo;
		double ysumi, ysumo;
		Rectangle2D cur;
		ysumi=0;
		ysumo=0;
		maxi=0;
		maxo=0;
		for (VisualContact c: inputs) {
			GlyphVector gv = c.getLabelGlyphs(g);
			cur = gv.getVisualBounds();
			maxi=(cur.getWidth()>maxi)?cur.getWidth():maxi;
			ysumi=ysumi+cur.getHeight();
		}
		for (VisualContact c: outputs) {
			GlyphVector gv = c.getLabelGlyphs(g);
			cur = gv.getVisualBounds();
			maxo=(cur.getWidth()>maxo)?cur.getWidth():maxo;
			ysumo=ysumo+cur.getHeight();
		}
		double height = Math.max(ysumo, ysumi);
		double width  = maxo+0.1+maxi;

		return new Rectangle2D.Double(-width/2, -height/2, width, height);
	}

	protected void drawContactsInLocalSpace(Graphics2D g, Rectangle2D BBox) {
		double maxi=0;
		double ysumi, cury;
		Rectangle2D cur;
		ysumi=0;

		cury=-ysumi/2;
		for (VisualContact c: inputs) {
			GlyphVector gv = c.getLabelGlyphs(g);

			cur = gv.getVisualBounds();
			maxi=(cur.getWidth()>maxi)?cur.getWidth():maxi;
			cury=cury+cur.getHeight();

			g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));


//			g.drawGlyphVector(gv, (float)labelPosition.getX(), (float)labelPosition.getY());
		}


	}


	@Override
	protected void drawInLocalSpace(Graphics2D g) {

		drawLabelInLocalSpace(g);

		Rectangle2D shape = getContactLabelBB(g);

		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(shape);

		drawContactsInLocalSpace(g, shape);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}


	@Override
	public Touchable hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (getBoundingBoxInLocalSpace().contains(pointInLocalSpace))
			return this;
		else
			return null;
	}

}
