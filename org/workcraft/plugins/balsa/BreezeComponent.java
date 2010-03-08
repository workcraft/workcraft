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

package org.workcraft.plugins.balsa;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.VisualComponentGeneratorAttribute;
import org.workcraft.dom.math.MathNode;
import org.workcraft.parsers.breeze.Block;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
@VisualComponentGeneratorAttribute(generator="org.workcraft.plugins.balsa.BreezeVisualComponentGenerator")
public class BreezeComponent extends MathNode implements Block<HandshakeComponent> {

	private org.workcraft.plugins.balsa.components.Component underlyingComponent;
	public BreezeComponent() {
	}

	public void setUnderlyingComponent(org.workcraft.plugins.balsa.components.Component underlyingComponent) {
		this.underlyingComponent = underlyingComponent;
	}

	public org.workcraft.plugins.balsa.components.Component getUnderlyingComponent() {
		return underlyingComponent;
	}

	public void setHandshakeComponents(Map<Handshake, HandshakeComponent> handshakes) {
		this.handshakeComponents = handshakes;
	}

	public final HandshakeComponent getHandshakeComponentByName(String name) {
		final Handshake hc = getHandshakes().get(name);
		if(hc == null)
			return null;
		return getHandshakeComponents().get(hc);
	}

	public Map<Handshake, HandshakeComponent> getHandshakeComponents() {
		return handshakeComponents;
	}

	public void setHandshakes(Map<String, Handshake> handshakes) {
		this.handshakes = handshakes;
	}

	public Map<String, Handshake> getHandshakes() {
		return handshakes;
	}

	private Map<Handshake, HandshakeComponent> handshakeComponents;
	private Map<String, Handshake> handshakes;

	@Override
	public List<HandshakeComponent> getPorts() {
		return Arrays.asList(handshakeComponents.values().toArray(new HandshakeComponent[0]));
	}
}
