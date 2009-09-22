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
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class ImplicitPlaceArc extends VisualConnection {
	private Place implicitPlace;
	private MathConnection refCon1;
	private MathConnection refCon2;

	private static double tokenSpaceSize = 0.8;
	private static double singleTokenSize = tokenSpaceSize / 1.9;
	private static double multipleTokenSeparation = 0.0125;
	private static Color tokenColor = Color.BLACK;

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration ("Tokens", "getTokens", "setTokens", int.class));
		addPropertyDeclaration(new PropertyDeclaration ("Capacity", "getCapacity", "setCapacity", int.class));

		/*addPopupMenuSegment(new PopupMenuBuilder.PopupMenuSegment() {
			public void addItems(JPopupMenu menu,
					ScriptedActionListener actionListener) {
				ScriptedActionMenuItem addToken = new ScriptedActionMenuItem(new VisualPlace.AddTokenAction(implicitPlace));
				addToken.addScriptedActionListener(actionListener);

				ScriptedActionMenuItem removeToken = new ScriptedActionMenuItem(new VisualPlace.RemoveTokenAction(implicitPlace));
				removeToken.addScriptedActionListener(actionListener);

				menu.add(new JLabel ("Implicit place"));
				menu.addSeparator();
				menu.add(addToken);
				menu.add(removeToken);
			}
		});*/
	}

	public ImplicitPlaceArc () {
		super();
		addPropertyDeclarations();
	}

	public void setImplicitPlaceArc (VisualComponent first, VisualComponent second, MathConnection refCon1, MathConnection refCon2, Place implicitPlace) {
		super.setDependencies(first, second, null);
		this.refCon1 = refCon1;
		this.refCon2 = refCon2;
		this.implicitPlace = implicitPlace;


		addPlaceObserver(implicitPlace);
	}

	private void addPlaceObserver(Place implicitPlace) {
		implicitPlace.addObserver( new StateObserver() {
				public void notify(StateEvent e) {
					observableStateImpl.sendNotification(e);
				}
			});
	}

	public ImplicitPlaceArc (VisualComponent first, VisualComponent second, MathConnection refCon1, MathConnection refCon2, Place implicitPlace) {
		super(null, first, second);
		this.refCon1 = refCon1;
		this.refCon2 = refCon2;
		this.implicitPlace = implicitPlace;

		addPlaceObserver(implicitPlace);


		addPropertyDeclarations();
	}

	public void draw(Graphics2D g) {
		super.draw(g);

		int tokens = implicitPlace.getTokens();

		Point2D p = getPointOnConnection(0.5);

		g.translate(p.getX(), p.getY());
		VisualPlace.drawTokens(tokens, singleTokenSize, multipleTokenSeparation, tokenSpaceSize, 0, Coloriser.colorise(tokenColor, getColorisation()), g);
	}

	@NoAutoSerialisation
	public int getTokens() {
		return implicitPlace.getTokens();
	}

	@NoAutoSerialisation
	public void setTokens(int tokens) {
		implicitPlace.setTokens(tokens);
	}

	@NoAutoSerialisation
	public int getCapacity() {
		return implicitPlace.getCapacity();
	}

	@NoAutoSerialisation
	public void setCapacity(int c) {
		implicitPlace.setCapacity(c);
	}

	@NoAutoSerialisation
	public Place getImplicitPlace() {
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

}
