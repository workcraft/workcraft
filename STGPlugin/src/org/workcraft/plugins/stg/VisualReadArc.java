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

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;

public class VisualReadArc extends VisualConnection {
	private MathConnection refCon1;
	private MathConnection refCon2;

	public VisualReadArc () {
		this(null, null, null, null);
	}

	public VisualReadArc (VisualComponent first, VisualComponent second,
			MathConnection refCon1, MathConnection refCon2) {
		super(null, first, second);
		this.refCon1 = refCon1;
		this.refCon2 = refCon2;
		setLineWidth(0.01);
		removePropertyDeclarations();
	}

	private void removePropertyDeclarations() {
		removePropertyDeclarationByName(VisualConnection.PROPERTY_ARROW_LENGTH);
		removePropertyDeclarationByName(VisualConnection.PROPERTY_ARROW_WIDTH);
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
		ret.add(refCon1);
		ret.add(refCon2);
		return ret;
	}

	public void setDependencies(MathConnection refCon1, MathConnection refCon2) {
		this.refCon1 = refCon1;
		this.refCon2 = refCon2;
	}

	@Override
	public boolean hasArrow() {
		return false;
	}

}
