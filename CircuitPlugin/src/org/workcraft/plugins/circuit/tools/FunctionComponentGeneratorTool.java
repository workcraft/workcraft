package org.workcraft.plugins.circuit.tools;

import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;

public class FunctionComponentGeneratorTool extends NodeGeneratorTool {

    public FunctionComponentGeneratorTool() {
        super(new DefaultNodeGenerator(FunctionComponent.class) {
            @Override
            public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
                VisualFunctionComponent component = (VisualFunctionComponent) super.generate(model, where);
                VisualFunctionContact contact = new VisualFunctionContact(new FunctionContact(IOType.OUTPUT));
                contact.setPosition(new Point2D.Double(0, 0));
                component.addContact((VisualCircuit) model, contact);
                model.setName(contact.getReferencedComponent(), "z");
                return component;
            }
        });
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create a component, then right-click on the component to add contacts.";
    }

}
