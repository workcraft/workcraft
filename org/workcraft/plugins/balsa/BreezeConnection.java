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

import org.workcraft.dom.math.MathConnection;

public class BreezeConnection extends MathConnection {

	private String handshake1;
	private String handshake2;

	public BreezeConnection(BreezeComponent first, BreezeComponent second) {
		super(first, second);
	}

	public void setHandshake1(String handshake1) {
		this.handshake1 = handshake1;
	}

	public String getHandshake1() {
		return handshake1;
	}

	public void setHandshake2(String handshake2) {
		this.handshake2 = handshake2;
	}

	public String getHandshake2() {
		return handshake2;
	}

}
