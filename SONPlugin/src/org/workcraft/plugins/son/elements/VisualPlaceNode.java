package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.tools.ErrTracingDisable;
import org.workcraft.plugins.son.tools.PlaceNodeDecoration;


public class VisualPlaceNode extends VisualComponent{

	private Font errorFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.45f);
	private Positioning errLabelPositioning = SONSettings.getErrLabelPositioning();
	private RenderedText errorRenderedText = new RenderedText("", errorFont, errLabelPositioning, new Point2D.Double(0.0,0.0));
	private Color errLabelColor = SONSettings.getErrLabelColor();
	private String value = "";

	protected static double singleTokenSize = CommonVisualSettings.getBaseSize() / 1.9;

	public VisualPlaceNode(PlaceNode refNode) {
		super(refNode);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualPlaceNode, Boolean>(
				this, "marked", Boolean.class) {
			public void setter(VisualPlaceNode object, Boolean value) {
				setMarked(value);
			}
			public Boolean getter(VisualPlaceNode object) {
				return isMarked();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualPlaceNode, Positioning>(
				this, "Error Positioning", Positioning.class) {
			protected void setter(VisualPlaceNode object, Positioning value) {
				object.setErrLabelPositioning(value);
			}
			protected Positioning getter(VisualPlaceNode object) {
				return object.getErrLabelPositioning();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualPlaceNode, Color>(
				this, "Error color", Color.class) {
			protected void setter(VisualPlaceNode object, Color value) {
				object.setErrLabelColor(value);
			}
			protected Color getter(VisualPlaceNode object) {
				return object.getErrLabelColor();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualPlaceNode, String>(
				this, "interface", String.class, true, true, false) {
			protected void setter(VisualPlaceNode object, String value) {
				object.setInterface(value);
			}
			protected String getter(VisualPlaceNode object) {
				return object.getInterface();
			}
		});
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();

		Shape shape = new Ellipse2D.Double(
				-getSize() / 2 + getStrokeWidth() / 2,
				-getSize() / 2 + getStrokeWidth() / 2,
				getSize() - getStrokeWidth(),
				getSize() - getStrokeWidth());

		g.setColor(Coloriser.colorise(getFillColor(), r.getDecoration().getBackground()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation()));
		g.setStroke(new BasicStroke((float)getStrokeWidth()));
		g.draw(shape);

		PlaceNode p = (PlaceNode)getReferencedComponent();
		boolean token = p.isMarked();
		if (d instanceof PlaceNodeDecoration) {
			token = ((PlaceNodeDecoration)d).hasToken();
		}
		drawToken(r, token, getSingleTokenSize(), Coloriser.colorise(getTokenColor(), r.getDecoration().getColorisation()));
		drawErrorInLocalSpace(r);
		drawLabelInLocalSpace(r);
		drawNameInLocalSpace(r);
	}

	public static void drawToken (DrawRequest r, boolean b, double singleTokenSize, Color tokenColor) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		if(b){
		Shape shape;
			shape = new Ellipse2D.Double(
					-singleTokenSize / 2,
					-singleTokenSize / 2,
					singleTokenSize,
					singleTokenSize);
			g.setColor(Coloriser.colorise(tokenColor, d.getColorisation()));
			g.fill(shape);
		}
	}

	private void cahceErrorRenderedText(DrawRequest r) {
		String error = "Err = "+((Integer)this.getErrors()).toString();
		//double o = 0.8 * size;

		Point2D offset = getOffset(errLabelPositioning);
		if (errLabelPositioning.ySign<0) {
			offset.setLocation(offset.getX(), offset.getY()-0.4);
		} else {
			offset.setLocation(offset.getX(), offset.getY()+0.4);
		}

		if (errorRenderedText.isDifferent(error, labelFont, errLabelPositioning, offset)) {
			errorRenderedText = new RenderedText(error, labelFont, errLabelPositioning, offset);
		}
	}

	protected void drawErrorInLocalSpace(DrawRequest r) {
		if (ErrTracingDisable.showErrorTracing()) {
			cahceErrorRenderedText(r);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			g.setColor(Coloriser.colorise(errLabelColor, d.getColorisation()));
			errorRenderedText.draw(g);
		}
	}

	@Override
	public void cacheRenderedText(DrawRequest r) {
		super.cacheRenderedText(r);
		cahceErrorRenderedText(r);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D bb = super.getBoundingBoxInLocalSpace();

//		if (ErrTracingDisable.showErrorTracing()){
//			bb = BoundingBoxHelper.union(bb, errorRenderedText.getBoundingBox());
//		}

		if (ErrTracingDisable.showErrorTracing()&&errorRenderedText!=null) {
			bb = BoundingBoxHelper.union(bb, errorRenderedText.getBoundingBox());
		}

		//super.getBoundingBoxInLocalSpace();
		return bb;
	}

	@Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return new Rectangle2D.Double(-getSize() / 2, -getSize() / 2, getSize(),getSize());
    }

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return pointInLocalSpace.distanceSq(0, 0) < getSize() * getSize() / 4;
	}

	public double getSize(){
		return size;
	}

	public double getStrokeWidth(){
		return strokeWidth;
	}

	public double getSingleTokenSize(){
		return singleTokenSize;
	}

	public boolean isMarked() {
		return ((PlaceNode)getReferencedComponent()).isMarked();
	}

	public void setMarked(boolean b) {
		((PlaceNode)getReferencedComponent()).setMarked(b);
	}

	public int getErrors(){
		return ((PlaceNode)getReferencedComponent()).getErrors();
	}

	public void setErrors(int errors){
		((PlaceNode)getReferencedComponent()).setErrors(errors);
	}

	public Color getTokenColor() {
		return ((PlaceNode)getReferencedComponent()).getTokenColor();
	}

	public void setTokenColor(Color tokenColor) {
		((PlaceNode)getReferencedComponent()).setTokenColor(tokenColor);
	}

	public Color getForegroundColor() {
		return ((PlaceNode)getReferencedComponent()).getForegroundColor();
	}

	public void setForegroundColor(Color foregroundColor) {
		((PlaceNode)getReferencedComponent()).setForegroundColor(foregroundColor);
	}

	public void setFillColor(Color fillColor){
		((PlaceNode)getReferencedComponent()).setFillColor(fillColor);
	}

	public Color getFillColor(){
		return ((PlaceNode)getReferencedComponent()).getFillColor();
	}

	public void setLabel(String label){
		super.setLabel(label);
		((PlaceNode)getReferencedComponent()).setLabel(label);
	}

	public String getLabel(){
		super.getLabel();
		return ((PlaceNode)getReferencedComponent()).getLabel();
	}

	public void setInterface(String value){
		this.value = value;
		sendNotification(new PropertyChangedEvent(this, "interface"));
	}

	public String getInterface(){
		return value;
	}

	public Positioning getErrLabelPositioning() {
		return errLabelPositioning;
	}

	public void setErrLabelPositioning(Positioning errorPositioning) {
		this.errLabelPositioning = errorPositioning;
		sendNotification(new PropertyChangedEvent(this, "Error positioning"));
	}

	public Color getErrLabelColor(){
		return this.errLabelColor;
	}

	public void setErrLabelColor(Color errLabelColor){
		this.errLabelColor = errLabelColor;
	}

	@Override
	public void copyStyle(Stylable src) {
		super.copyStyle(src);
		if (src instanceof VisualPlaceNode) {
			VisualPlaceNode srcComponent = (VisualPlaceNode)src;
			setMarked(srcComponent.isMarked());
			setErrLabelPositioning(srcComponent.getErrLabelPositioning());
			setErrLabelColor(srcComponent.getErrLabelColor());
			setInterface(srcComponent.getInterface());
		}
	}
}
