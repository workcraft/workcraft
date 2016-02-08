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

package org.workcraft.plugins.fsm.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class EventDeserialiser implements CustomXMLDeserialiser {
    @Override
    public String getClassName() {
        return Event.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser) throws DeserialisationException {

        MathNode first = (MathNode)internalReferenceResolver.getObject(element.getAttribute("first"));
        MathNode second = (MathNode)internalReferenceResolver.getObject(element.getAttribute("second"));
        Symbol symbol = (Symbol)internalReferenceResolver.getObject(element.getAttribute("symbol"));

        Event event = (Event) instance;
        event.setDependencies(first, second);
        event.setSymbol(symbol);
    }

    @Override
    public Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new Event();
    }

    @Override
    public void initInstance(Element element, Object instance,
            ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {

    }
}