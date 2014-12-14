/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualImplicitPlaceArc extends VisualConnection {
	private STGPlace implicitPlace;
	private MathConnection refCon1;
	private MathConnection refCon2;

	private static Color[] TokenColors = {
		Color.RED, Color.GREEN, Color.BLUE,
		Color.RED.brighter(), Color.GREEN.brighter(), Color.BLUE.brighter(),
		Color.RED.darker(), Color.GREEN.darker(), Color.BLUE.darker() };

	private static double tokenSpaceSize = 0.8;
	private static double singleTokenSize = tokenSpaceSize / 1.9;
	private static double multipleTokenSeparation = 0.0125;

	protected Color tokenColor = CommonVisualSettings.getBorderColor();
	private boolean isTokenColorGenerator = false;
	private int tokenColorIndex = 0;


	public VisualImplicitPlaceArc () {
		super();
		addPropertyDeclarations();
	}

	public VisualImplicitPlaceArc (VisualComponent first, VisualComponent second,
			MathConnection refCon1, MathConnection refCon2, STGPlace implicitPlace) {
		super(null, first, second);
		this.refCon1 = refCon1;
		this.refCon2 = refCon2;
		this.implicitPlace = implicitPlace;

		addPlaceObserver(implicitPlace);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualImplicitPlaceArc, Integer>(
				this, "Tokens", Integer.class) {
			public void setter(VisualImplicitPlaceArc object, Integer value) {
				object.getImplicitPlace().setTokens(value);
			}
			public Integer getter(VisualImplicitPlaceArc object) {
				return object.getImplicitPlace().getTokens();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualImplicitPlaceArc, Integer>(
				this, "Capacity", Integer.class) {
			public void setter(VisualImplicitPlaceArc object, Integer value) {
				object.getImplicitPlace().setCapacity(value);
			}
			public Integer getter(VisualImplicitPlaceArc object) {
				return object.getImplicitPlace().getCapacity();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualImplicitPlaceArc, Color>(
				this, "Token color", Color.class) {
			public void setter(VisualImplicitPlaceArc object, Color value) {
				object.setTokenColor(value);
			}
			public Color getter(VisualImplicitPlaceArc object) {
				return object.getTokenColor();
			}
		});
	}

	private void addPlaceObserver(Place implicitPlace) {
		implicitPlace.addObserver( new StateObserver() {
			public void notify(StateEvent e) {
				observableStateImpl.sendNotification(e);
			}
		});
	}

	public void setImplicitPlaceArcDependencies (MathConnection refCon1, MathConnection refCon2, STGPlace implicitPlace) {
		this.refCon1 = refCon1;
		this.refCon2 = refCon2;
		this.implicitPlace = implicitPlace;

		addPlaceObserver(implicitPlace);
	}

	@Override
	public void draw(DrawRequest r) {
		super.draw(r);
		int tokens = implicitPlace.getTokens();
		Point2D p = getPointOnConnection(0.5);
		r.getGraphics().translate(p.getX(), p.getY());
		VisualPlace.drawTokens(r, tokens, singleTokenSize, multipleTokenSeparation, tokenSpaceSize, 0, tokenColor);
	}

	@NoAutoSerialisation
	public STGPlace getImplicitPlace() {
		return implicitPlace;
	}

	public MathConnection getRefCon1() {
		return refCon1;
	}

	public MathConnection getRefCon2() {
		return refCon2;
	}

	@Override
	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(implicitPlace);
		ret.add(refCon1);
		ret.add(refCon2);
		return ret;
	}

	public boolean isTokenColorGenerator() {
		return this.isTokenColorGenerator;
	}

	public void setTokenColorGenerator(boolean value) {
		this.isTokenColorGenerator = value;
	}

	public void renewTokenColor(boolean random) {
		int nextTokenColorIndex = 0;
		if (!random) {
			nextTokenColorIndex = (tokenColorIndex+1) % TokenColors.length;
		} else {
			do {
				nextTokenColorIndex = (int)(Math.random() * TokenColors.length);
			} while (nextTokenColorIndex == tokenColorIndex);
		}
		tokenColorIndex = nextTokenColorIndex;
		setTokenColor(TokenColors[tokenColorIndex]);
	}

	public Color getTokenColor() {
		return tokenColor;
	}

	public void setTokenColor(Color tokenColor) {
		this.tokenColor = tokenColor;
		sendNotification(new PropertyChangedEvent(this, "token color"));
	}

	@Override
	public void copyProperties(VisualConnection src) {
		super.copyProperties(src);
		if (src instanceof VisualImplicitPlaceArc) {
			VisualImplicitPlaceArc srcImplicitPlaceArc = (VisualImplicitPlaceArc)src;
			setTokenColor(srcImplicitPlaceArc.getTokenColor());
			STGPlace srcPlace = srcImplicitPlaceArc.getImplicitPlace();
			getImplicitPlace().setTokens(srcPlace.getTokens());
			getImplicitPlace().setCapacity(srcPlace.getCapacity());
		}
	}
}
