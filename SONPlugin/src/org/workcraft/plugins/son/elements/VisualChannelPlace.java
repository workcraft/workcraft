package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;


import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.elements.ChannelPlace;



@DisplayName("ChannelPlace")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/channel-place.svg")
public class VisualChannelPlace extends VisualPlace{

	public VisualChannelPlace(ChannelPlace cplace) {
		super(cplace);
	}

	@Override
	public void draw(DrawRequest r){
		Graphics2D g = r.getGraphics();

		drawLabelInLocalSpace(r);

		double size = CommonVisualSettings.getSize()*1.2;
		double strokeWidth = CommonVisualSettings.getStrokeWidth()*2.0;

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

		Place c = (Place)getReferencedComponent();

		drawTokens(c.getTokens(), singleTokenSize, multipleTokenSeparation, size, strokeWidth,
				Coloriser.colorise(getTokenColor(), r.getDecoration().getColorisation()), g);
	}

	public Color getForegroundColor() {
		return ((ChannelPlace)getReferencedComponent()).getForegroundColor();
	}

	public void setForegroundColor(Color foregroundColor) {
		((ChannelPlace)getReferencedComponent()).setForegroundColor(foregroundColor);
	}

	public void setFillColor(Color fillColor){
		((ChannelPlace)getReferencedComponent()).setFillColor(fillColor);
	}

	public Color getFillColor(){
		return ((ChannelPlace)getReferencedComponent()).getFillColor();
	}

	public void setLabel(String label){
		super.setLabel(label);
		((ChannelPlace)getReferencedComponent()).setLabel(label);
	}

	public String getLabel(){
		super.getLabel();
		return ((ChannelPlace)getReferencedComponent()).getLabel();
	}


}
