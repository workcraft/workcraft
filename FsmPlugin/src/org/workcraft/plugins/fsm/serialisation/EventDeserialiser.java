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

        MathNode first = (MathNode) internalReferenceResolver.getObject(element.getAttribute("first"));
        MathNode second = (MathNode) internalReferenceResolver.getObject(element.getAttribute("second"));
        Symbol symbol = (Symbol) internalReferenceResolver.getObject(element.getAttribute("symbol"));

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