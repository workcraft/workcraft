package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.tools.ConditionDecoration;
import org.workcraft.plugins.son.tools.ErrTracingDisable;



@DisplayName("Condition")
@Hotkey(KeyEvent.VK_B)
@SVGIcon("images/icons/svg/place_empty.svg")
public class VisualCondition extends VisualComponent{

	private Font errorFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.45f);
	private Positioning errLabelPositioning = SONSettings.getErrLabelPositioning();
	private RenderedText errorRenderedText = new RenderedText("", errorFont, errLabelPositioning, 0.0);
	private Color errLabelColor = SONSettings.getErrLabelColor();

	protected static double singleTokenSize = CommonVisualSettings.getBaseSize() / 1.9;
	private Color tokenColor = CommonVisualSettings.getBorderColor();

	public VisualCondition(Condition condition){
		super(condition);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualCondition, Boolean>(
				this, "Token", Boolean.class) {
			public void setter(VisualCondition object, Boolean value) {
				((Condition)getReferencedComponent()).setMarked(value);
			}
			public Boolean getter(VisualCondition object) {
				return ((Condition)getReferencedComponent()).isMarked();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualCondition, Positioning>(
				this, "Error Positioning", Positioning.class, Positioning.getChoice()) {
			protected void setter(VisualCondition object, Positioning value) {
				object.setErrLabelPositioning(value);
			}
			protected Positioning getter(VisualCondition object) {
				return object.getErrLabelPositioning();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualCondition, Color>(
				this, "Error color", Color.class) {
			protected void setter(VisualCondition object, Color value) {
				object.setErrLabelColor(value);
			}
			protected Color getter(VisualCondition object) {
				return object.getErrLabelColor();
			}
		});
	}

	@Override
	public void draw(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();

		Shape shape = new Ellipse2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Coloriser.colorise(getFillColor(), r.getDecoration().getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation()));
		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(shape);

		Condition p = (Condition)getReferencedComponent();
		boolean token = p.isMarked();
		if (d instanceof ConditionDecoration) {
			token = ((ConditionDecoration)d).hasToken();
		}
		drawToken(r, token, singleTokenSize, Coloriser.colorise(getTokenColor(), r.getDecoration().getColorisation()));

		drawLabelInLocalSpace(r);
		drawNameInLocalSpace(r);
		drawErrorInLocalSpace(r);
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
		double offset = 0.8 * size;
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
		Rectangle2D bb = new Rectangle2D.Double(-size / 2, -size / 2, size,	size);
		if (ErrTracingDisable.showErrorTracing()){
			bb = BoundingBoxHelper.union(bb, errorRenderedText.getBoundingBox());
		}
		super.getBoundingBoxInLocalSpace();
		return bb;
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
	}

	public boolean hasToken() {
		return ((Condition)getReferencedComponent()).isMarked();
	}

	public void setToken(boolean b) {
		((Condition)getReferencedComponent()).setMarked(b);
	}

	public int getErrors(){
		return ((Condition)getReferencedComponent()).getErrors();
	}

	public void setErrors(int errors){
		((Condition)getReferencedComponent()).setErrors(errors);
	}

	public Color getTokenColor() {
		return tokenColor;
	}

	public void setTokenColor(Color tokenColor) {
		this.tokenColor = tokenColor;
	}

	public Color getForegroundColor() {
		return ((Condition)getReferencedComponent()).getForegroundColor();
	}

	public void setForegroundColor(Color foregroundColor) {
		((Condition)getReferencedComponent()).setForegroundColor(foregroundColor);
	}

	public void setFillColor(Color fillColor){
		((Condition)getReferencedComponent()).setFillColor(fillColor);
	}

	public Color getFillColor(){
		return ((Condition)getReferencedComponent()).getFillColor();
	}

	public void setLabel(String label){
		super.setLabel(label);
		((Condition)getReferencedComponent()).setLabel(label);
	}

	public String getLabel(){
		super.getLabel();
		return ((Condition)getReferencedComponent()).getLabel();
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
}
