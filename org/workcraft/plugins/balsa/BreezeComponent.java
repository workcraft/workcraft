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
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

@VisualClass("org.workcraft.plugins.balsa.VisualBreezeComponent")
@VisualComponentGeneratorAttribute(generator="org.workcraft.plugins.balsa.BreezeVisualComponentGenerator")
public class BreezeComponent extends MathNode implements Block<BreezeHandshake> {

	private DynamicComponent underlyingComponent;
	public BreezeComponent() {
	}

	public void setUnderlyingComponent(DynamicComponent underlyingComponent) {
		this.underlyingComponent = underlyingComponent;
	}

	public DynamicComponent getUnderlyingComponent() {
		return underlyingComponent;
	}

	public void setHandshakeComponents(Map<Handshake, BreezeHandshake> handshakes) {
		this.handshakeComponents = handshakes;
	}

	public final BreezeHandshake getHandshakeComponentByName(String name) {
		final Handshake hc = getHandshakes().get(name);
		if(hc == null)
			return null;
		return getHandshakeComponents().get(hc);
	}

	public Map<Handshake, BreezeHandshake> getHandshakeComponents() {
		return handshakeComponents;
	}

	public void setHandshakes(Map<String, Handshake> handshakes) {
		this.handshakes = handshakes;
	}

	public Map<String, Handshake> getHandshakes() {
		return handshakes;
	}

	private Map<Handshake, BreezeHandshake> handshakeComponents;
	private Map<String, Handshake> handshakes;

	@Override
	public List<BreezeHandshake> getPorts() {
		return Arrays.asList(handshakeComponents.values().toArray(new BreezeHandshake[0]));
	}
}
