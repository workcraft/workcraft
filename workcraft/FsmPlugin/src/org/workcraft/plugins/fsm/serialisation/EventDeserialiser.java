package org.workcraft.plugins.fsm.serialisation;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;

public class EventDeserialiser implements CustomXMLDeserialiser<Event> {

    @Override
    public String getClassName() {
        return Event.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Event instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        MathNode first = (MathNode) internalReferenceResolver.getObject(element.getAttribute("first"));
        MathNode second = (MathNode) internalReferenceResolver.getObject(element.getAttribute("second"));
        Symbol symbol = (Symbol) internalReferenceResolver.getObject(element.getAttribute("symbol"));

        instance.setDependencies(first, second);
        instance.setSymbol(symbol);
    }

    @Override
    public Event createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new Event();
    }

    @Override
    public void initInstance(Element element, Event instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {

    }

}