package org.workcraft.presets;

import org.w3c.dom.Element;

public interface DataSerialiser<T> {
    T fromXML(Element element);
    void toXML(T data, Element parent);
}
