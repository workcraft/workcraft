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

    public static final String PROPERTY_NAME = "Name";
    public static final String PROPERTY_IO_TYPE = "I/O type";
    public static final String PROPERTY_SIGNAL_LEVEL = "Signal level";
    public static final String PROPERTY_INITIALISED = "Initialised";

    public enum IOType {
        INPUT("Input"),
        OUTPUT("Output");

        private final String name;

        IOType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    public enum SignalLevel {
        LOW("Low"),
        HIGH("High");

        private final String name;

        SignalLevel(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    private String name = "";
    private IOType type = IOType.OUTPUT;
    private SignalLevel level = SignalLevel.LOW;
    private boolean initialised = false;

    public Contact() {
    }

    public Contact(IOType ioType) {
        super();
        setIOType(ioType);
    }

    // FIXME: This setName method is only to enable accessing contact name via getName. Use setName of Circuit class to set all node names!
    public void setName(String value) {
        if (!this.name.equals(value)) {
            this.name = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME));
        }
    }

    public String getName() {
        return name;
    }

    public void setIOType(IOType value) {
        if (this.type != value) {
            this.type = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IO_TYPE));
        }
    }

    public IOType getIOType() {
        return type;
    }

    public SignalLevel getSignalLevel() {
        return level;
    }

    public void setSignalLevel(SignalLevel value) {
        if (this.level != value) {
            this.level = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_SIGNAL_LEVEL));
        }
    }

    public void setSignalLevel(boolean value) {
        if (value) {
            setSignalLevel(SignalLevel.HIGH);
        } else {
            setSignalLevel(SignalLevel.LOW);
        }
    }

    public boolean getInitialised() {
        return initialised;
    }

    public void setInitialised(boolean value) {
        if (this.initialised != value) {
            this.initialised = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INITIALISED));
        }
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
        return getIOType() == IOType.INPUT;
    }

    public boolean isOutput() {
        return getIOType() == IOType.OUTPUT;
    }

    public boolean isPort() {
        return !(getParent() instanceof CircuitComponent);
    }

    public boolean isDriver() {
        return isOutput() != isPort();
    }

    public boolean isDriven() {
        return isOutput() == isPort();
    }

}
