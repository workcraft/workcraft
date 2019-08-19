package org.workcraft.plugins.xbm.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.xbm.*;
import org.workcraft.plugins.xbm.utils.CommonXbmSerialistionUtil;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;
import org.workcraft.serialisation.ReferenceResolver;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class BurstEventDeserialiser implements CustomXMLDeserialiser<BurstEvent> {

    @Override
    public String getClassName() {
        return BurstEvent.class.getName();
    }

    @Override
    public BurstEvent createInstance(Element element, ReferenceResolver externalReferenceResolver, Object... constructorParameters) {
        return new BurstEvent();
    }

    @Override
    public void initInstance(Element element, BurstEvent instance, ReferenceResolver externalReferenceResolver, NodeInitialiser nodeInitialiser) throws DeserialisationException {
    }

    @Override
    public void finaliseInstance(Element element, BurstEvent instance, ReferenceResolver internalReferenceResolver, ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException {
        int dirCounter = 0;
        Burst burst = new Burst();
        XbmState from = (XbmState) internalReferenceResolver.getObject(element.getAttribute(CommonXbmSerialistionUtil.BURST_FROM_STATE));
        XbmState to = (XbmState) internalReferenceResolver.getObject(element.getAttribute(CommonXbmSerialistionUtil.BURST_TO_STATE));
        while (internalReferenceResolver.getObject(element.getAttribute(CommonXbmSerialistionUtil.BURST_DIRECTION_SIGNAL + dirCounter)) != null) {
            Signal signal = (Signal) internalReferenceResolver.getObject(element.getAttribute(CommonXbmSerialistionUtil.BURST_DIRECTION_SIGNAL + dirCounter));
            Burst.Direction direction = Burst.Direction.convertFromString(element.getAttribute(CommonXbmSerialistionUtil.BURST_DIRECTION_VALUE + dirCounter));
            burst.addOrChangeSignalDirection(signal, direction);
            ++dirCounter;
        }
        burst.setFrom(from);
        burst.setTo(to);
        instance.setBurst(burst);
        instance.setDependencies(from, to);
    }
}