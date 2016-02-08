package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.visual.AbstractVisualModel;

public class TitlePropertyDescriptor implements PropertyDescriptor {
    public static final String PROPERTY_TITLE = "Title";

    private final AbstractVisualModel model;

    public TitlePropertyDescriptor(AbstractVisualModel model) {
        this.model = model;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return model.getTitle();
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
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

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return false;
    }

    @Override
    public boolean isTemplatable() {
        return false;
    }

}
