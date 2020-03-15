package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.*;

import java.awt.geom.Point2D;

public class FunctionComponentGeneratorTool extends NodeGeneratorTool {

    public FunctionComponentGeneratorTool() {
        super(new DefaultNodeGenerator(FunctionComponent.class) {
            @Override
            public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
                VisualFunctionComponent component = (VisualFunctionComponent) super.generate(model, where);
                VisualFunctionContact contact = new VisualFunctionContact(new FunctionContact(IOType.OUTPUT));
                component.addContact(contact);
                component.setPositionByDirection(contact, VisualContact.Direction.EAST, false);
                return component;
            }
        });
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create a component, then right-click on the component to add contacts.";
    }

}
