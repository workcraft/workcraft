package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.workcraft.dom.Connection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;

public class ImplicitPlaceArc extends VisualConnection {
	private Place implicitPlace;
	private Connection refCon1;
	private Connection refCon2;

	private static double tokenSpaceSize = 0.8;
	private static double singleTokenSize = tokenSpaceSize / 1.9;
	private static double multipleTokenSeparation = 0.0125;


	private static Color tokenColor = Color.BLACK;


	public ImplicitPlaceArc (VisualComponent first, VisualComponent second, Connection refCon1, Connection refCon2, Place implicitPlace) {
		super(null, first, second);
		this.refCon1 = refCon1;
		this.refCon2 = refCon2;
		this.implicitPlace = implicitPlace;

		addPropertyDeclaration(new PropertyDeclaration ("Tokens", "getTokens", "setTokens", int.class));
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);

		int tokens = implicitPlace.getTokens();

		Point2D p = getPointOnConnection(0.5);

		g.translate(p.getX(), p.getY());
		VisualPlace.drawTokens(tokens, singleTokenSize, multipleTokenSeparation, tokenSpaceSize, 0, Coloriser.colorise(tokenColor, getColorisation()), g);
	}

	public int getTokens() {
		return implicitPlace.getTokens();
	}

	public void setTokens(int tokens) {
		implicitPlace.setTokens(tokens);
	}

	public Place getImplicitPlace() {
		return implicitPlace;
	}

	public Connection getRefCon1() {
		return refCon1;
	}

	public Connection getRefCon2() {
		return refCon2;
	}


}
