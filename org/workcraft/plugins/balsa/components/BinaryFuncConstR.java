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

import java.util.BitSet;

public class BinaryFuncConstR extends Component {
	private int outputWidth;
	private int inputWidth;
	private BinaryOperator op;
	private boolean outputIsSigned;
	private boolean inputIsSigned;
	private boolean constIsSigned;
	private BitSet constant;

	public void setOutputWidth(int outputWidth) {
		this.outputWidth = outputWidth;
	}
	public int getOutputWidth() {
		return outputWidth;
	}
	public void setInputWidth(int inputWidth) {
		this.inputWidth = inputWidth;
	}
	public int getInputWidth() {
		return inputWidth;
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
	public void setInputIsSigned(boolean inputIsSigned) {
		this.inputIsSigned = inputIsSigned;
	}
	public boolean getInputIsSigned() {
		return inputIsSigned;
	}
	public void setConstIsSigned(boolean constIsSigned) {
		this.constIsSigned = constIsSigned;
	}
	public boolean getConstIsSigned() {
		return constIsSigned;
	}
	public void setConstant(BitSet constant) {
		this.constant = constant;
	}
	public BitSet getConstant() {
		return constant;
	}
}
