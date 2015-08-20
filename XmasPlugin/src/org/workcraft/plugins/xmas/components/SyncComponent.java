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

package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.VisualClass;

@VisualClass(org.workcraft.plugins.xmas.components.VisualSyncComponent.class)
public class SyncComponent extends XmasComponent {

	public String gp1 = "0";
	public String gp2 = "0";
	public String typ = "a";

	public void setGp1(String gp1) {
		this.gp1 = gp1;
	}

	public String getGp1() {
		return gp1;
	}

	public void setGp2(String gp2) {
		this.gp2 = gp2;
	}

	public String getGp2() {
		return gp2;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getTyp() {
		return typ;
	}

}