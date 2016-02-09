package org.workcraft.plugins.son.elements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.util.Interval;


@Hotkey(KeyEvent.VK_E)
@DisplayName ("Event")
@SVGIcon("images/icons/svg/transition.svg")

public class VisualEvent extends VisualTransition implements VisualTransitionNode{
    //private boolean displayName = false;

    public VisualEvent(Event event) {
        super(event);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualEvent, Boolean>(
                this, "Fault", Boolean.class, true, true, true) {
            public void setter(VisualEvent object, Boolean value) {
                ((Event)getReferencedComponent()).setFaulty(value);
            }
            public Boolean getter(VisualEvent object) {
                return     ((Event)getReferencedComponent()).isFaulty();
            }
        });
    }

    @Override
    public void draw(DrawRequest r){
        super.draw(r);
        drawFault(r);
    }

    public void drawFault(DrawRequest r){
        if (SONSettings.isErrorTracing()) {
            Graphics2D g = r.getGraphics();
            GlyphVector glyphVector=null;
            Rectangle2D labelBB=null;

            Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(0.5f);

            if (isFaulty())
                glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "1");
            else
                glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "0");

            labelBB = glyphVector.getVisualBounds();
            Point2D bitPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getCenterY());
            g.drawGlyphVector(glyphVector, -(float)bitPosition.getX(), -(float)bitPosition.getY());
        }
    }

    public Event getMathTransitionNode(){
        return (Event)this.getReferencedComponent();
    }

    public void setLabel(String label){
        super.setLabel(label);
        ((Event)getReferencedComponent()).setLabel(label);
    }

    public String getLabel(){
        super.getLabel();
        return ((Event)getReferencedComponent()).getLabel();
    }

    public void setFaulty(Boolean fault){
        ((Event)getReferencedComponent()).setFaulty(fault);
    }

    public boolean isFaulty(){
        return ((Event)getReferencedComponent()).isFaulty();
    }

    public Color getForegroundColor() {
        return ((Event)getReferencedComponent()).getForegroundColor();
    }

    public void setForegroundColor(Color foregroundColor) {
        ((Event)getReferencedComponent()).setForegroundColor(foregroundColor);
    }

    public void setFillColor(Color fillColor){
        ((Event)getReferencedComponent()).setFillColor(fillColor);
    }

    public Color getFillColor(){
        return ((Event)getReferencedComponent()).getFillColor();
    }

    public void setStartTime(String time){
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event)getReferencedComponent()).setStartTime(input);
    }

    public String getStartTime(){
        return ((Event)getReferencedComponent()).getStartTime().toString();
    }

    public void setEndTime(String time){
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event)getReferencedComponent()).setEndTime(input);
    }

    public String getEndTime(){
        return ((Event)getReferencedComponent()).getEndTime().toString();
    }

    public void setDuration(String time) {
        Interval input = new Interval(Interval.getMin(time), Interval.getMax(time));
        ((Event)getReferencedComponent()).setDuration((input));
    }

    public String getDuration() {
        return ((Event)getReferencedComponent()).getDuration().toString();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualEvent) {
            VisualEvent srcComponent = (VisualEvent)src;
            setFaulty(srcComponent.isFaulty());
        }
    }
}
