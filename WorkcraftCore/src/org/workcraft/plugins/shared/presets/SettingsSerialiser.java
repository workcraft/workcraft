package org.workcraft.plugins.shared.presets;

import org.w3c.dom.Element;

public interface SettingsSerialiser<T> {
    T fromXML(Element e);
    void toXML(T settings, Element parent);
}
