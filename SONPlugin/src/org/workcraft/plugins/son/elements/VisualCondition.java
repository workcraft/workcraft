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

    private Positioning startTimePositioning = Positioning.LEFT;
    private RenderedText startTimeRenderedText = new RenderedText("", timeFont, startTimePositioning, new Point2D.Double(0.0,0.0));

    private Positioning endTimePositioning = Positioning.RIGHT;
    private RenderedText endTimeRenderedText = new RenderedText("", timeFont, endTimePositioning, new Point2D.Double(0.0,0.0));

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
                this, "Final", Boolean.class, false, false, false) {
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

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

    private void cahceStartTimeRenderedText(DrawRequest r) {
        String start = "Start: "+ getStartTime().toString();

        Point2D offset = getOffset(startTimePositioning);
        offset.setLocation(offset.getX(), offset.getY() - labelOffset);

        if (startTimeRenderedText.isDifferent(start, timeFont, startTimePositioning, offset)) {
            startTimeRenderedText = new RenderedText(start, timeFont, startTimePositioning, offset);
        }
    }

    protected void drawStartTimeInLocalSpace(DrawRequest r) {
        if (isInitial() && SONSettings.getTimeVisibility() && ((Condition)getReferencedComponent()).getStartTime().isSpecified()) {
            cahceStartTimeRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(Coloriser.colorise(getStartTimeColor(), d.getColorisation()));
            startTimeRenderedText.draw(g);
        }
    }

    private void cahceEndTimeRenderedText(DrawRequest r) {
        String end = "End: "+ getEndTime().toString();

        Point2D offset = getOffset(endTimePositioning);
        offset.setLocation(offset.getX(), offset.getY() - labelOffset);

        if (endTimeRenderedText.isDifferent(end, timeFont, endTimePositioning, offset)) {
            endTimeRenderedText = new RenderedText(end, timeFont, endTimePositioning, offset);
        }
    }

    protected void drawEndTimeInLocalSpace(DrawRequest r) {
        if (isFinal() && SONSettings.getTimeVisibility() && ((Condition)getReferencedComponent()).getEndTime().isSpecified()) {
            cahceEndTimeRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(Coloriser.colorise(getEndTimeColor(), d.getColorisation()));
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

        if (isInitial() && SONSettings.getTimeVisibility() && ((Condition)getReferencedComponent()).getStartTime().isSpecified()) {
            bb = BoundingBoxHelper.union(bb, startTimeRenderedText.getBoundingBox());
        }

        if (isFinal() && SONSettings.getTimeVisibility() && ((Condition)getReferencedComponent()).getEndTime().isSpecified()) {
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

    public Color getStartTimeColor(){
        return ((Condition)getReferencedComponent()).getStartTimeColor();
    }

    public void setStartTimeColor(Color value){
        ((Condition)getReferencedComponent()).setStartTimeColor(value);
    }

    public Color getEndTimeColor(){
        return ((Condition)getReferencedComponent()).getEndTimeColor();
    }

    public void setEndTimeColor(Color value){
        ((Condition)getReferencedComponent()).setEndTimeColor(value);
    }

}
