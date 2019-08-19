package org.workcraft.plugins.xbm.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.xbm.*;
import org.workcraft.plugins.xbm.utils.CommonXbmSerialistionUtil;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

import java.util.Map;

public class BurstEventSerialiser implements CustomXMLSerialiser<BurstEvent> {

    @Override
    public String getClassName() {
        return BurstEvent.class.getName();
    }

    @Override
    public void serialise(Element element, BurstEvent object, ReferenceProducer internalReferences, ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException {
        int dirCounter = 0;
        Burst burst = object.getBurst();
        element.setAttribute(CommonXbmSerialistionUtil.BURST_FROM_STATE, internalReferences.getReference(burst.getFrom()));
        element.setAttribute(CommonXbmSerialistionUtil.BURST_TO_STATE, internalReferences.getReference(burst.getTo()));
        for (Map.Entry<Signal, Burst.Direction> entry: burst.getDirection().entrySet()) {
            element.setAttribute(CommonXbmSerialistionUtil.BURST_DIRECTION_SIGNAL + dirCounter, internalReferences.getReference(entry.getKey()));
            element.setAttribute(CommonXbmSerialistionUtil.BURST_DIRECTION_VALUE + dirCounter, entry.getValue().toString());
            ++dirCounter;
        }
    }
}