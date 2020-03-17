package org.workcraft.presets;

import org.w3c.dom.Element;

public class TextDataSerialiser implements DataSerialiser<String> {

    @Override
    public String fromXML(Element element) {
        return element.getTextContent();
    }

    @Override
    public void toXML(String data, Element element) {
        element.setTextContent(data);
    }

}
