package org.workcraft.plugins.shared;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

public class MpsatPreset {
	private String description;
	private MpsatSettings settings;
	private boolean builtIn;

	public MpsatPreset(String description, MpsatSettings settings, boolean builtIn) {
		this.description = description;
		this.settings = settings;
		this.builtIn = builtIn;
	}

	public MpsatPreset(String description, MpsatSettings settings) {
		this (description, settings, false);
	}

	public MpsatPreset(Element e) {
		this.description = XmlUtil.readStringAttr(e, "description");
		this.settings = new MpsatSettings(XmlUtil.getChildElement("settings", e));
		this.builtIn = false;
	}

	public String getDescription() {
		return description;
	}

	public MpsatSettings getSettings() {
		return settings;
	}

	void setDescription(String description) {
		this.description = description;
	}

	void setSettings(MpsatSettings settings) {
		this.settings = settings;
	}

	public boolean isBuiltIn() {
		return builtIn;
	}

	public String toString() {
		return description + (builtIn? " [built-in]" : "");
	}
}
