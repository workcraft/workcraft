package org.workcraft.gui.properties;

import org.workcraft.dom.visual.AbstractVisualModel;

import java.util.Map;

public class TitlePropertyDescriptor implements PropertyDescriptor {

    public static final String PROPERTY_TITLE = "Title";

    private final AbstractVisualModel model;

    public TitlePropertyDescriptor(AbstractVisualModel model) {
        this.model = model;
    }

    @Override
    public Object getValue() {
        return model.getTitle();
    }

    @Override
    public void setValue(Object value) {
        model.setTitle(value.toString());
    }

    @Override
    public Map<? extends Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return PROPERTY_TITLE;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

}
