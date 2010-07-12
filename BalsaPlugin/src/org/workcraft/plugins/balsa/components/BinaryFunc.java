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

public class BinaryFunc extends Component {
	private int outputWidth;
	private int inputAWidth;
	private int inputBWidth;
	private BinaryOperator op;
	private boolean outputIsSigned;
	private boolean inputAIsSigned;
	private boolean inputBIsSigned;

	public void setOutputWidth(int outputWidth) {
		this.outputWidth = outputWidth;
	}
	public int getOutputWidth() {
		return outputWidth;
	}
	public void setInputAWidth(int inputAWidth) {
		this.inputAWidth = inputAWidth;
	}
	public int getInputAWidth() {
		return inputAWidth;
	}
	public void setInputBWidth(int inputBWidth) {
		this.inputBWidth = inputBWidth;
	}
	public int getInputBWidth() {
		return inputBWidth;
	}
	public void setOp(BinaryOperator op) {
		this.op = op;
	}
	public BinaryOperator getOp() {
		return op;
	}
	public void setOutputIsSigned(boolean outputIsSigned) {
		this.outputIsSigned = outputIsSigned;
	}
	public boolean getOutputIsSigned() {
		return outputIsSigned;
	}
	public void setInputAIsSigned(boolean inputAIsSigned) {
		this.inputAIsSigned = inputAIsSigned;
	}
	public boolean getInputAIsSigned() {
		return inputAIsSigned;
	}
	public void setInputBIsSigned(boolean inputBIsSigned) {
		this.inputBIsSigned = inputBIsSigned;
	}
	public boolean getInputBIsSigned() {
		return inputBIsSigned;
	}
}
