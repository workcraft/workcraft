package org.workcraft.plugins.xbm.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.xbm.XbmSignal;
import org.workcraft.plugins.xbm.SignalState;
import org.workcraft.plugins.xbm.XbmState;
import org.workcraft.plugins.xbm.utils.CommonXbmSerialistionUtil;
import org.workcraft.serialisation.*;

public class EncodingDeserialiser implements CustomXMLDeserialiser<XbmState> {

    @Override
    public String getClassName() {
        return XbmState.class.getName();
    }

    @Override
    public XbmState createInstance(Element element, ReferenceResolver externalReferenceResolver, Object... constructorParameters) {

        return new XbmState();
    }

    @Override
    public void initInstance(Element element, XbmState instance, ReferenceResolver externalReferenceResolver, NodeInitialiser nodeInitialiser) {
    }

    //FIXME The supervisor hierarchy fixed the issue with new state generationafter running the simulation tool
    //FIXME However, the copy and paste is still broken
    @Override
    public void finaliseInstance(Element element, XbmState instance, ReferenceResolver internalReferenceResolver, ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {
        int counter = 0;
        while (internalReferenceResolver.getObject(element.getAttribute(CommonXbmSerialistionUtil.ENCODING_SIGNAL + counter)) != null) {
            XbmSignal xbmSignal = (XbmSignal) internalReferenceResolver.getObject(element.getAttribute(CommonXbmSerialistionUtil.ENCODING_SIGNAL + counter));
            SignalState signalValue = SignalState.convertFromString(element.getAttribute(CommonXbmSerialistionUtil.ENCODING_VALUE + counter));
            instance.addOrChangeSignalValue(xbmSignal, signalValue);
            ++counter;
        }
    }
}
