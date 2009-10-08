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

package org.workcraft.dom.math;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;


@VisualClass("org.workcraft.dom.visual.connections.VisualConnection")
public class MathConnection extends MathNode implements Connection {
	private MathNode first;
	private MathNode second;

	public MathConnection () {
	}

	public MathConnection (MathNode first, MathNode second) {
		super();
		setDependencies(first, second);
	}

	final public MathNode getFirst() {
		return first;
	}

	final public MathNode getSecond() {
		return second;
	}

	final public void setDependencies(MathNode first, MathNode second) {
		this.first = first;	 this.second = second;
	}

	public String toString() {
		return "MathConnection " + this.hashCode() + " (" + first +", " + second +")";
	}
}