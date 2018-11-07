package org.workcraft.gui.properties;

import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;

import java.util.Map;

public class NamePropertyDescriptor implements PropertyDescriptor {

    public static final String PROPERTY_NAME = "Name";

    private final AbstractVisualModel model;
    private final VisualNode node;

    public NamePropertyDescriptor(AbstractVisualModel model, VisualNode node) {
        this.model = model;
        this.node = node;
    }

    @Override
    public Object getValue() {
        return model.getMathName(node);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof String) {
            String name = (String) value;
            if (Identifier.isName(name)) {
                if (!name.equals(model.getMathName(node))) {
                    model.setMathName(node, name);
                    node.sendNotification(new PropertyChangedEvent(node, PROPERTY_NAME));
                }
            } else {
                throw new ArgumentException("'" + name + "' is not a valid C-style identifier.\n"
                        + "The first character must be alphabetic or '_' and the following -- alphanumeric or '_'.");
            }
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

}
