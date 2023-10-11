package org.workcraft.plugins.mpsat_verification.presets;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

import java.util.HashSet;

public class LocalSelfTriggeringDataSerialiser implements DataSerialiser<LocalSelfTriggeringParameters> {

    @Override
    public LocalSelfTriggeringParameters fromXML(Element parent, LocalSelfTriggeringParameters defaultParameters) {
        return new LocalSelfTriggeringParameters(new HashSet<>(XmlUtils.readList(parent)));
    }

    @Override
    public void toXML(LocalSelfTriggeringParameters parameters, Element parent) {
        XmlUtils.writeList(parameters.getOrderedExceptionSignals(), parent);
    }

}
