package org.workcraft.presets;

import org.w3c.dom.Element;
import org.workcraft.utils.XmlUtils;

public class Preset<T> {
    private String description;
    private T settings;
    private final boolean builtIn;

    public Preset(String description, T settings, boolean builtIn) {
        this.description = description;
        this.settings = settings;
        this.builtIn = builtIn;
    }

    public Preset(Element e, SettingsSerialiser<T> serialiser) {
        this.description = XmlUtils.readStringAttr(e, "description");
        this.settings = serialiser.fromXML(XmlUtils.getChildElement("settings", e));
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

    @Override
    public String toString() {
        return description.trim() + (builtIn ? " [built-in]" : "");
    }
}
