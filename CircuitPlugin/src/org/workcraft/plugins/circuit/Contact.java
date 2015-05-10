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

package org.workcraft.plugins.circuit;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor;

@VisualClass(org.workcraft.plugins.circuit.VisualContact.class)
public class Contact extends MathNode implements BooleanVariable {

	public static final String PROPERTY_INIT_TO_ONE = "init to one";
	public static final String PROPERTY_IO_TYPE = "IO type";

	public static final String PROPERTY_NAME = "name";
	public enum IOType {
		INPUT("Input"),
		OUTPUT("Output");

		private final String name;

		private IOType(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	};

	private boolean initToOne = false;
	private IOType ioType = IOType.OUTPUT;
	private String name = "";

	public Contact() {
	}

	public Contact(IOType ioType) {
		super();
		setIOType(ioType);
	}

	public boolean getInitToOne() {
		return initToOne;
	}

	public void setInitToOne(boolean value) {
		if (this.initToOne != value) {
			this.initToOne = value;
			sendNotification(new PropertyChangedEvent(this, PROPERTY_INIT_TO_ONE));
		}
	}

	public void setIOType(IOType t) {
		if (this.ioType != t) {
			this.ioType = t;
			sendNotification(new PropertyChangedEvent(this, PROPERTY_IO_TYPE));
		}
	}

	public IOType getIOType() {
		return ioType;
	}

	// this is only for information, use Circuit to set component's names
	public void setName(String value) {
		if (!this.name.equals(value)) {
			this.name = value;
			sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME));
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public String getLabel() {
		return getName();
	}

	public boolean isInput() {
		return (getIOType() == IOType.INPUT);
	}

	public boolean isOutput() {
		return (getIOType() == IOType.OUTPUT);
	}

	public boolean isPort() {
		return !(getParent() instanceof CircuitComponent);
	}

	public boolean isDriver() {
//		return (isOutput() || isPort());
		return (isOutput() != isPort());
	}

	public boolean isDriven() {
		return (isOutput() == isPort());
	}

}
