package org.workcraft.plugins.mpsat_verification.presets;

import org.w3c.dom.Element;
import org.workcraft.presets.DataSerialiser;
import org.workcraft.utils.XmlUtils;

import java.util.HashSet;

public class InputPropernessDataSerialiser implements DataSerialiser<InputPropernessParameters> {

    @Override
    public InputPropernessParameters fromXML(Element parent, InputPropernessParameters defaultParameters) {
        return new InputPropernessParameters(new HashSet<>(XmlUtils.readList(parent)));
    }

    @Override
    public void toXML(InputPropernessParameters parameters, Element parent) {
        XmlUtils.writeList(parameters.getOrderedExceptionSignals(), parent);
    }

}
