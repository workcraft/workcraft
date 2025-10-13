package org.workcraft.presets;

import org.w3c.dom.Element;
import org.workcraft.utils.XmlUtils;

import java.util.List;

public class ListDataSerialiser implements DataSerialiser<List<String>> {

    @Override
    public List<String> fromXML(Element parent, List<String> defaultParameters) {
        return XmlUtils.readItems(parent);
    }

    @Override
    public void toXML(List<String> parameters, Element parent) {
        XmlUtils.writeItems(parameters, parent);
    }

}
