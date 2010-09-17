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

package org.workcraft.plugins.balsa.layouts;

import java.util.Map;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stg.ComponentStgBuilder;
import org.workcraft.plugins.balsa.stg.implementations.StgBuilderSelector;

public class MainLayouter {

	public static HandshakeComponentLayout getLayout(DynamicComponent component, Map<String, Handshake> handshakes)
	{
		ComponentStgBuilder<DynamicComponent> stgBuilder = StgBuilderSelector.create(component.declaration().getName());

		HandshakeComponentLayout layout = stgBuilder.getLayout(component, handshakes);

		int totalSize = 0;
		totalSize += layout.getTop() != null ? 1 : 0;
		totalSize += layout.getBottom() != null ? 1 : 0;
		for(Handshake[] block : layout.getLeft())
			totalSize += block.length;
		for(Handshake[] block : layout.getRight())
			totalSize += block.length;

		if(totalSize != handshakes.size())
		{
			System.err.println("Bad layout! Only " + totalSize + " handshakes were laid out instead of " + handshakes.size());
			return getDefaultLayout(handshakes);
		}

		return layout;
	}

	private static HandshakeComponentLayout getDefaultLayout(
			final Map<String, Handshake> handshakes) {
		return new HandshakeComponentLayout()
		{
			public Handshake getBottom() {
				return null;
			}

			public Handshake[][] getLeft() {
				return new Handshake[][]{new Handshake[]{}};
			}

			public Handshake[][] getRight() {
				Handshake[] allHandshakes = new Handshake[handshakes.size()];
				handshakes.values().toArray(allHandshakes);
				return new Handshake[][]{allHandshakes};
			}

			public Handshake getTop() {
				return null;
			}
		};
	}
}
