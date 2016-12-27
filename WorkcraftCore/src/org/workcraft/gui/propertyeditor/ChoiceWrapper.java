package org.workcraft.gui.propertyeditor;

public class ChoiceWrapper {
    public Object value;
    public String text;

    public ChoiceWrapper(String text, Object value) {
        this.text = text;
        this.value = value;
    }

    @Override
    public String toString() {
        return text;
    }
}
