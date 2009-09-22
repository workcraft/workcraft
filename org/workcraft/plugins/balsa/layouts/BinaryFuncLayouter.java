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

import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;

public class BinaryFuncLayouter extends Layouter<BinaryFunc> {

	@Override
	HandshakeComponentLayout getLayout(BinaryFunc component, final Map<String, Handshake> handshakes) {
		return new HandshakeComponentLayout()
		{
			public Handshake getBottom() {
				return null;
			}
			public Handshake getTop() {
				return null;
			}
			public Handshake[][] getLeft() {
				return new Handshake[][]{new Handshake[]{handshakes.get("inpA"), handshakes.get("inpB")}};
			}
			public Handshake[][] getRight() {
				return new Handshake[][]{new Handshake[]{handshakes.get("out")}};
			}
		};
	}

}
