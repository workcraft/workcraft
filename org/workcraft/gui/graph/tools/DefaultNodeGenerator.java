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

package org.workcraft.gui.graph.tools;

import org.workcraft.NodeFactory;
import org.workcraft.annotations.Annotations;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;

public class DefaultNodeGenerator extends AbstractNodeGenerator {

	private Class<?> cls;
	private Class<?> vcls;
	private String displayName;
	private int hk;

	public DefaultNodeGenerator (Class<?> cls) {
		this.cls = cls;
		this.vcls = Annotations.getVisualClass(cls);
		this.displayName = Annotations.getDisplayName(vcls);
		this.hk = Annotations.getHotKeyCode(vcls);
	}

	@Override
	protected MathNode createMathNode() throws NodeCreationException {
		return NodeFactory.createNode(cls);
	}

	@Override
	public int getHotKeyCode() {
		return hk;
	}

	@Override
	public String getLabel() {
		return displayName;
	}
}
