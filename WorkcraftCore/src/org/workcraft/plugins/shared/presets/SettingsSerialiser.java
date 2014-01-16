package org.workcraft.plugins.shared.presets;

import org.w3c.dom.Element;

public interface SettingsSerialiser<T> {
	public T fromXML(Element e);
	public void toXML(T settings, Element parent);
}
