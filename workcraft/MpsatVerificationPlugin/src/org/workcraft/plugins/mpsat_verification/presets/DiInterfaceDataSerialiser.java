package org.workcraft.plugins.mpsat_verification.presets;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

public class DiInterfaceDataSerialiser implements DataSerialiser<DiInterfaceParameters> {

    @Override
    public DiInterfaceParameters fromXML(Element parent, DiInterfaceParameters defaultParameters) {
        return new DiInterfaceParameters(XmlUtils.readItemLists(parent));
    }

    @Override
    public void toXML(DiInterfaceParameters parameters, Element parent) {
        XmlUtils.writeItemLists(parameters.getOrderedExceptionSignalSets(), parent);
    }

}
