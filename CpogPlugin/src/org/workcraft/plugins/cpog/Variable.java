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

package org.workcraft.plugins.cpog;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.cpog.VisualVariable.class)
public class Variable extends MathNode implements Comparable<Variable>, BooleanVariable {

    public static final String PROPERTY_STATE = "State";
    public static final String PROPERTY_LABEL = "Label";

    private VariableState state = VariableState.UNDEFINED;

    private String label = "";

    public void setState(VariableState state) {
        this.state = state;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_STATE));
    }

    public VariableState getState() {
        return state;
    }

    @Override
    public int compareTo(Variable o) {
        return label.compareTo(o.label);
    }

    public void setLabel(String label) {
        this.label = label;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL));
    }

    public String getLabel() {
        return label;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
