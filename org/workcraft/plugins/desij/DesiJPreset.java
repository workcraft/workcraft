/**
 *
 */
package org.workcraft.plugins.desij;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

/**
 * @author Dominic Wist
 *
 */
public class DesiJPreset {
	private String description;
	private DesiJSettings settings; // represents the parameters for DesiJ
	private boolean builtIn;

	// all Constructors to build a DesiJ preset

	public DesiJPreset(String description, DesiJSettings settings, boolean builtIn) {
		this.description = description;
		this.settings = settings;
		this.builtIn = builtIn;
	}

	public DesiJPreset(String description, DesiJSettings settings) {
		this (description, settings, false);
	}

	public DesiJPreset(Element e) {
		this.description = XmlUtil.readStringAttr(e, "description");
		this.settings = new DesiJSettings(XmlUtil.getChildElement("settings", e));
		this.builtIn = false;
	}

	// Getters for all other classes

	public String getDescription() {
		return description;
	}

	public DesiJSettings getSettings() {
		return settings;
	}

	public boolean isBuiltIn() {
		return builtIn;
	}

	// Setters for classes within the same package

	void setDescription(String description) {
		this.description = description;
	}

	void setSettings(DesiJSettings settings) {
		this.settings = settings;
	}

	// for output into a Combobox

	public String toString() {
		return description + (builtIn? " [built-in]" : "");
	}
}
