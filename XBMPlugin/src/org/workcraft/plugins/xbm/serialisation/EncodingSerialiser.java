package org.workcraft.plugins.xbm.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.xbm.Signal;
import org.workcraft.plugins.xbm.SignalState;
import org.workcraft.plugins.xbm.XbmState;
import org.workcraft.plugins.xbm.utils.CommonXbmSerialistionUtil;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

import java.util.Map;

public class EncodingSerialiser implements CustomXMLSerialiser<XbmState> {

    @Override
    public String getClassName() {
        return XbmState.class.getName();
    }

    @Override
    public void serialise(Element element, XbmState object, ReferenceProducer internalReferences, ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException {
        int counter = 0;
        for (Map.Entry<Signal, SignalState> entry: object.getEncoding().entrySet()) {
            element.setAttribute(CommonXbmSerialistionUtil.ENCODING_SIGNAL + counter, internalReferences.getReference(entry.getKey()));
            element.setAttribute(CommonXbmSerialistionUtil.ENCODING_VALUE + counter, entry.getValue().toString());
            ++counter;
        }
    }
}
