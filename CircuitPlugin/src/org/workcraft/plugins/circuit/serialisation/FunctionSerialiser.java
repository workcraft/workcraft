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

package org.workcraft.plugins.circuit.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.cpog.serialisation.BooleanFormulaSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class FunctionSerialiser implements CustomXMLSerialiser {
    public static final String RESET_FUNCTION_ATTRIBUTE_NAME = "resetFunction";
    public static final String SET_FUNCTION_ATTRIBUTE_NAME = "setFunction";

    @Override
    public String getClassName() {
        return FunctionContact.class.getName();
    }

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
            throws SerialisationException {
        FunctionContact function = (FunctionContact) object;

        BooleanFormulaSerialiser.writeFormulaAttribute(element, internalReferences,
                function.getSetFunction(), SET_FUNCTION_ATTRIBUTE_NAME);

        BooleanFormulaSerialiser.writeFormulaAttribute(element, internalReferences,
                function.getResetFunction(), RESET_FUNCTION_ATTRIBUTE_NAME);
    }

}
