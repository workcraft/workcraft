package org.workcraft.plugins.mpsat_verification.presets;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

public class OutputPersistencyDataSerialiser implements DataSerialiser<OutputPersistencyParameters> {

    @Override
    public OutputPersistencyParameters fromXML(Element parent, OutputPersistencyParameters defaultParameters) {
        return new OutputPersistencyParameters(XmlUtils.readItemLists(parent));
    }

    @Override
    public void toXML(OutputPersistencyParameters parameters, Element parent) {
        XmlUtils.writeItemLists(parameters.getOrderedExceptionSignalSets(), parent);
    }

}
