package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Node;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;

public class NamePropertyDescriptor implements PropertyDescriptor {
    public static final String PROPERTY_NAME = "Name";

    private final AbstractModel model;
    private final Node node;

    public NamePropertyDescriptor(AbstractModel model, Node node) {
        this.model = model;
        this.node = node;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return model.getName(node);
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        model.setName(node, (String) value);
        if (node instanceof ObservableState) {
            ((ObservableState) node).sendNotification(new PropertyChangedEvent(node, PROPERTY_NAME));
        }
    }

    @Override
    public Map<? extends Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
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
