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

package org.workcraft.plugins.balsa.components;

public class CallDemuxPush extends Component {
	private int width;
	private int outputCount;

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setOutputCount(int outputCount) {
		this.outputCount = outputCount;
	}

	public int getOutputCount() {
		return outputCount;
	}
}
