package org.workcraft.presets;

import org.w3c.dom.Element;

public class TextDataSerialiser implements DataSerialiser<String> {

    @Override
    public String fromXML(Element parent, String defaultData) {
        return parent == null ? defaultData : parent.getTextContent();
    }

    @Override
    public void toXML(String data, Element parent) {
        if (parent != null) {
            parent.setTextContent(data);
        }
    }

}
