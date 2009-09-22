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

public class Split extends Component {
	private int inputWidth;
	private int lsOutputWidth;
	private int msOutputWidth;
	public void setInputWidth(int inputWidth) {
		this.inputWidth = inputWidth;
	}
	public int getInputWidth() {
		return inputWidth;
	}
	public void setLsOutputWidth(int lsOutputWidth) {
		this.lsOutputWidth = lsOutputWidth;
	}
	public int getLsOutputWidth() {
		return lsOutputWidth;
	}
	public void setMsOutputWidth(int msOutputWidth) {
		this.msOutputWidth = msOutputWidth;
	}
	public int getMsOutputWidth() {
		return msOutputWidth;
	}
}
