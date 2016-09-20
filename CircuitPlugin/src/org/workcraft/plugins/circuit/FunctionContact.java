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
import org.workcraft.formula.BooleanFormula;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.circuit.VisualFunctionContact.class)
public class FunctionContact extends Contact {
    public static final String PROPERTY_SET_FUNCTION = "Set function";
    public static final String PROPERTY_RESET_FUNCTION = "Reset function";

    private BooleanFormula setFunction = null;
    private BooleanFormula resetFunction = null;

    public FunctionContact(IOType ioType) {
        super(ioType);
    }

    public FunctionContact() {
        super();
    }

    public BooleanFormula getSetFunction() {
        return setFunction;
    }

    public void setSetFunction(BooleanFormula value) {
        setSetFunctionQuiet(value);
        sendNotification(new PropertyChangedEvent(this, PROPERTY_SET_FUNCTION));
    }

    public void setSetFunctionQuiet(BooleanFormula value) {
        setFunction = value;
    }

    public BooleanFormula getResetFunction() {
        return resetFunction;
    }

    public void setResetFunction(BooleanFormula value) {
        setResetFunctionQuiet(value);
        sendNotification(new PropertyChangedEvent(this, PROPERTY_RESET_FUNCTION));
    }

    public void setResetFunctionQuiet(BooleanFormula value) {
        resetFunction = value;
    }

}
