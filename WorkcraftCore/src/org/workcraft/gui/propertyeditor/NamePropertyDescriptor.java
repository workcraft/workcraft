package org.workcraft.gui.propertyeditor;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.AbstractModel;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.util.Identifier;

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
        String name = (String) value;
        if (Identifier.isName(name)) {
            model.setName(node, name);
            if (node instanceof ObservableState) {
                ((ObservableState) node).sendNotification(new PropertyChangedEvent(node, PROPERTY_NAME));
            }
        } else {
            throw new ArgumentException("'" + name + "' is not a valid C-style identifier.\n\n"
                    + "The first character must be alphabetic or an underscore and\n"
                    + "the following characters must be alphanumeric or an underscore.");
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
