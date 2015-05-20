package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.son.SONSettings;


@DisplayName("Condition")
@Hotkey(KeyEvent.VK_B)
@SVGIcon("images/icons/svg/place_empty.svg")
public class VisualCondition extends VisualPlaceNode{

	private static double size = 1.0;
	private static float strokeWidth = 0.1f;

	protected Color startTimeColor = Color.BLACK;
	private Positioning startTimePositioning = Positioning.LEFT;
	private RenderedText startTimeRenderedText = new RenderedText("", font, startTimePositioning, new Point2D.Double(0.0,0.0));

	protected Color endTimeColor = Color.BLACK;
	private Positioning endTimePositioning = Positioning.RIGHT;
	private RenderedText endTimeRenderedText = new RenderedText("", font, endTimePositioning, new Point2D.Double(0.0,0.0));

	public VisualCondition(Condition refNode) {
		super(refNode);
		addPropertyDeclarations();
	}

	//uneditable properties
	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualCondition, Boolean>(
				this, "Initial", Boolean.class, false, false, false) {
			public void setter(VisualCondition object, Boolean value) {
				setInitial(value);
			}
			public Boolean getter(VisualCondition object) {
				return isInitial();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualCondition, Boolean>(
				this, "Final", Boolean.class, false, true, true) {
			public void setter(VisualCondition object, Boolean value) {
				setFinal(value);
			}
			public Boolean getter(VisualCondition object) {
				return isFinal();
			}
		});
	}

	@Override
	public void draw(DrawRequest r) {
		super.draw(r);

		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();

		if (isInitial()) {
			double s = size/4;
			Path2D shape = new Path2D.Double();
			shape.moveTo(-size, 0.0);
			shape.lineTo(-size/2, 0.0);
			shape.moveTo(-size/2 - s, -s/2);
			shape.lineTo(-size/2, 0.0);
			shape.lineTo(-size/2 - s, s/2);
			g.setStroke(new BasicStroke(strokeWidth));
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
			g.draw(shape);
		}

		if (isFinal()) {
			double s = 2*size/3;
			Shape shape = new Ellipse2D.Double(-s/2, -s/2, s, s);
			g.setStroke(new BasicStroke(strokeWidth/2));
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
			g.draw(shape);
		}

		drawStartTimeInLocalSpace(r);
		drawEndTimeInLocalSpace(r);
	}

	private void cahceStartTimeRenderedText(DrawRequest r) {
		String start = "start: "+ getStartTime();

		Point2D offset = getOffset(startTimePositioning);
		offset.setLocation(offset.getX() , offset.getY() - labelOffset);

		if (startTimeRenderedText.isDifferent(start, font, startTimePositioning, offset)) {
			startTimeRenderedText = new RenderedText(start, font, startTimePositioning, offset);
		}
	}

	protected void drawStartTimeInLocalSpace(DrawRequest r) {
		if (isInitial() && SONSettings.getTimeVisibility()) {
			cahceStartTimeRenderedText(r);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			g.setColor(Coloriser.colorise(errLabelColor, d.getColorisation()));
			startTimeRenderedText.draw(g);
		}
	}

	private void cahceEndTimeRenderedText(DrawRequest r) {
		String end = "end: "+ getEndTime();
		//double o = 0.8 * size;

		Point2D offset = getOffset(endTimePositioning);
		offset.setLocation(offset.getX() , offset.getY() - labelOffset);

		if (endTimeRenderedText.isDifferent(end, font, endTimePositioning, offset)) {
			endTimeRenderedText = new RenderedText(end, font, endTimePositioning, offset);
		}
	}

	protected void drawEndTimeInLocalSpace(DrawRequest r) {
		if (isFinal() && SONSettings.getTimeVisibility()) {
			cahceEndTimeRenderedText(r);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			g.setColor(Coloriser.colorise(errLabelColor, d.getColorisation()));
			endTimeRenderedText.draw(g);
		}
	}

	@Override
	public void cacheRenderedText(DrawRequest r) {
		super.cacheRenderedText(r);
		cahceStartTimeRenderedText(r);
		cahceEndTimeRenderedText(r);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D bb = super.getBoundingBoxInLocalSpace();

		if (isInitial() && SONSettings.getTimeVisibility()) {
			bb = BoundingBoxHelper.union(bb, startTimeRenderedText.getBoundingBox());
		}

		if (isFinal() && SONSettings.getTimeVisibility()) {
			bb = BoundingBoxHelper.union(bb, endTimeRenderedText.getBoundingBox());
		}
		return bb;
	}

	public boolean isInitial() {
		return ((Condition)getReferencedComponent()).isInitial();
	}

	public void setInitial(boolean value) {
		((Condition)getReferencedComponent()).setInitial(value);
		sendNotification(new PropertyChangedEvent(this, "initial"));
	}

	public boolean isFinal() {
		return ((Condition)getReferencedComponent()).isFinal();
	}

	public void setFinal(boolean value) {
		((Condition)getReferencedComponent()).setFinal(value);
		sendNotification(new PropertyChangedEvent(this, "final"));
	}

	public String getStartTime(){
		return ((Condition)getReferencedComponent()).getStartTime();
	}

	public void setStartTime(String time){
		((Condition)getReferencedComponent()).setStartTime(time);
	}

	public String getEndTime(){
		return ((Condition)getReferencedComponent()).getEndTime();
	}

	public void setEndTime(String time){
		((Condition)getReferencedComponent()).setEndTime(time);
	}

	public Color getStartTimeColor(){
		return startTimeColor;
	}

	public void setStartTimeColor(Color value){
		startTimeColor = value;
	}

	public Color getEndTimeColor(){
		return endTimeColor;
	}

	public void setEndTimeColor(Color value){
		endTimeColor = value;
	}
}
