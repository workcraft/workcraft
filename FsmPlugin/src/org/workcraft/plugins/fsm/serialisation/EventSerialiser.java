package org.workcraft.plugins.fsm.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class EventSerialiser implements CustomXMLSerialiser {
    @Override
    public String getClassName() {
        return Event.class.getName();
    }

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
            throws SerialisationException {
        Event event = (Event) object;

        element.setAttribute("first", internalReferences.getReference(event.getFirst()));
        element.setAttribute("second", internalReferences.getReference(event.getSecond()));
        element.setAttribute("symbol", internalReferences.getReference(event.getSymbol()));
    }
}