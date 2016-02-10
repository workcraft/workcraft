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
import org.workcraft.plugins.son.tools.PlaceNodeDecoration;
import org.workcraft.plugins.son.util.Interval;


public class VisualPlaceNode extends VisualComponent{

    protected Font errorFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.45f);
    protected Font timeFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.35f);

    protected Positioning errLabelPositioning = Positioning.BOTTOM;
    protected RenderedText errorRenderedText = new RenderedText("", errorFont, errLabelPositioning, new Point2D.Double(0.0,0.0));
    protected Color errLabelColor = SONSettings.getErrLabelColor();

    private Positioning durationLabelPositioning = Positioning.BOTTOM;
    private RenderedText durationRenderedText = new RenderedText("", timeFont, durationLabelPositioning, new Point2D.Double(0.0,0.0));

    private String value = "";

    protected static double singleTokenSize = CommonVisualSettings.getBaseSize() / 1.9;
    protected static double labelOffset = 0.5;

    public VisualPlaceNode(PlaceNode refNode) {
        super(refNode);
        addPropertyDeclarations();

    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualPlaceNode, Boolean>(
                this, "Marking", Boolean.class, true, true, true) {
            public void setter(VisualPlaceNode object, Boolean value) {
                setIsMarked(value);
            }
            public Boolean getter(VisualPlaceNode object) {
                return isMarked();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualPlaceNode, Color>(
                this, "Error color", Color.class, true, true, true) {
            protected void setter(VisualPlaceNode object, Color value) {
                object.setErrLabelColor(value);
            }
            protected Color getter(VisualPlaceNode object) {
                return object.getErrLabelColor();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualPlaceNode, String>(
                this, "Block interface", String.class, false, true, false) {
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

        drawToken(r);
        drawErrorInLocalSpace(r);
        drawDurationInLocalSpace(r);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    public void drawToken(DrawRequest r) {

        PlaceNode p = (PlaceNode)getReferencedComponent();
        Decoration d = r.getDecoration();
        boolean token = p.isMarked();
        if (d instanceof PlaceNodeDecoration) {
            token = ((PlaceNodeDecoration)d).hasToken();
        }

        if(token){
        Graphics2D g = r.getGraphics();
        double singleTokenSize = getSingleTokenSize();
        Color tokenColor = Coloriser.colorise(getTokenColor(), r.getDecoration().getColorisation());

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
        String error = "Err = "+((Integer)getErrors()).toString();


        Point2D offset = getOffset(errLabelPositioning);
        if (errLabelPositioning.ySign<0) {
            offset.setLocation(offset.getX(), offset.getY() - labelOffset);
        } else {
            offset.setLocation(offset.getX(), offset.getY() + labelOffset);
        }

        if (errorRenderedText.isDifferent(error, errorFont, errLabelPositioning, offset)) {
            errorRenderedText = new RenderedText(error, errorFont, errLabelPositioning, offset);
        }
    }

    protected void drawErrorInLocalSpace(DrawRequest r) {
        if (SONSettings.isErrorTracing()) {
            cahceErrorRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(Coloriser.colorise(errLabelColor, d.getColorisation()));
            errorRenderedText.draw(g);
        }
    }

    private void cahceDurationRenderedText(DrawRequest r) {
        String duration = "D: "+ getDuration().toString();

        Point2D offset = getOffset(durationLabelPositioning);
        if (durationLabelPositioning.ySign<0) {
            offset.setLocation(offset.getX(), offset.getY() - labelOffset);
        } else {
            offset.setLocation(offset.getX(), offset.getY() + labelOffset);
        }

        if (durationRenderedText.isDifferent(duration, timeFont, durationLabelPositioning, offset)) {
            durationRenderedText = new RenderedText(duration, timeFont, durationLabelPositioning, offset);
        }
    }

    protected void drawDurationInLocalSpace(DrawRequest r) {
        if (SONSettings.getTimeVisibility() && ((PlaceNode)getReferencedComponent()).getDuration().isSpecified()) {
            cahceDurationRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(Coloriser.colorise(getDurationColor(), d.getColorisation()));
            durationRenderedText.draw(g);
        }
    }

    @Override
    public void cacheRenderedText(DrawRequest r) {
        super.cacheRenderedText(r);
        cahceErrorRenderedText(r);
        cahceDurationRenderedText(r);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();

        if (SONSettings.isErrorTracing()) {
            bb = BoundingBoxHelper.union(bb, errorRenderedText.getBoundingBox());
        }
        if (SONSettings.getTimeVisibility() && ((PlaceNode)getReferencedComponent()).getDuration().isSpecified()) {
            bb = BoundingBoxHelper.union(bb, durationRenderedText.getBoundingBox());
        }

        return bb;
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

    public void setIsMarked(boolean b) {
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
        sendNotification(new PropertyChangedEvent(this, "Interface"));
    }

    public String getInterface(){
        return value;
    }

    public Color getErrLabelColor(){
        return this.errLabelColor;
    }

    public void setErrLabelColor(Color errLabelColor){
        this.errLabelColor = errLabelColor;
    }

    public String getDuration(){
        return ((PlaceNode)getReferencedComponent()).getDuration().toString();
    }

    public void setDuration(String time){
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((PlaceNode)getReferencedComponent()).setDuration(input);
    }

    public String getStartTime(){
        return ((PlaceNode)getReferencedComponent()).getStartTime().toString();
    }

    public void setStartTime(String time){
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((PlaceNode)getReferencedComponent()).setStartTime(input);
    }

    public String getEndTime(){
        return ((PlaceNode)getReferencedComponent()).getEndTime().toString();
    }

    public void setEndTime(String time){
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((PlaceNode)getReferencedComponent()).setEndTime(input);
    }

    public Color getDurationColor(){
        return ((PlaceNode)getReferencedComponent()).getDurationColor();
    }

    public void setDurationColor(Color value){
        ((PlaceNode)getReferencedComponent()).setDurationColor(value);
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualPlaceNode) {
            VisualPlaceNode srcComponent = (VisualPlaceNode)src;
            setIsMarked(srcComponent.isMarked());
        }
    }
}
