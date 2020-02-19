package org.workcraft.presets;

import org.w3c.dom.Element;

public class Preset<T> {

    private String description;
    private T data;
    private final boolean builtIn;

    public Preset(String description, T data, boolean builtIn) {
        this.description = description;
        this.data = data;
        this.builtIn = builtIn;
    }

    public Preset(Element element, DataSerialiser<T> serialiser) {
        this.description = element.getAttribute(PresetManager.DESCRIPTION_ATTRIBUTE_NAME);
        this.data = serialiser.fromXML(element);
        this.builtIn = false;
    }

    public String getDescription() {
        return description;
    }

    public T getData() {
        return data;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    @Override
    public String toString() {
        return description.trim() + (builtIn ? " [built-in]" : "");
    }

}
