package org.workcraft.plugins.shared.presets;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public class Preset <T> {
	private String description;
	private T settings;
	private boolean builtIn;

	public Preset(String description, T settings, boolean builtIn) {
		this.description = description;
		this.settings = settings;
		this.builtIn = builtIn;
	}

	public Preset(Element e, SettingsSerialiser<T> serialiser) {
		this.description = XmlUtil.readStringAttr(e, "description");
		this.settings = serialiser.fromXML(XmlUtil.getChildElement("settings", e));
		this.builtIn = false;
	}

	public String getDescription() {
		return description;
	}

	public T getSettings() {
		return settings;
	}

	void setDescription(String description) {
		this.description = description;
	}

	void setSettings(T settings) {
		this.settings = settings;
	}

	public boolean isBuiltIn() {
		return builtIn;
	}

	public String toString() {
		return description + (builtIn? " [built-in]" : "");
	}
}
