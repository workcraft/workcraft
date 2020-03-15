package org.workcraft.plugins.fsm.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;

public class EventSerialiser implements CustomXMLSerialiser<Event> {

    @Override
    public String getClassName() {
        return Event.class.getName();
    }

    @Override
    public void serialise(Element element, Event object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        element.setAttribute("first", internalReferences.getReference(object.getFirst()));
        element.setAttribute("second", internalReferences.getReference(object.getSecond()));
        element.setAttribute("symbol", internalReferences.getReference(object.getSymbol()));
    }

}