package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.LibraryManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

public class FunctionComponentGeneratorTool extends NodeGeneratorTool {

    private static final JComboBox<String> gateComboBox = new JComboBox<>();

    public FunctionComponentGeneratorTool() {
        super(new DefaultNodeGenerator(FunctionComponent.class) {
            @Override
            public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
                VisualFunctionComponent component = (VisualFunctionComponent) super.generate(model, where);
                VisualFunctionContact contact = new VisualFunctionContact(new FunctionContact(IOType.OUTPUT));
                component.addContact(contact);
                component.setPositionByDirection(contact, VisualContact.Direction.EAST, false);

                Object selectedItem = gateComboBox.getSelectedItem();
                if ((selectedItem instanceof String) && (model instanceof VisualCircuit)) {
                    String name = (String) selectedItem;
                    VisualCircuit circuit = (VisualCircuit) model;
                    Gate gate = LibraryManager.getLibrary().get(name);
                    if (gate != null) {
                        GenlibUtils.convertToGate(circuit, component, gate);
                    }
                }
                return component;
            }
        });
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        gateComboBox.removeAllItems();
        gateComboBox.addItem(null);
        Library library = LibraryManager.getLibrary();
        if (library != null) {
            for (String gateName : library.getNames()) {
                gateComboBox.addItem(gateName);
            }
        }

        gateComboBox.addActionListener(e -> {
            Object item = gateComboBox.getSelectedItem();
            getTemplateNode().setLabel(item instanceof String ? (String) item : "");
            Framework.getInstance().updatePropertyView();
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(gateComboBox, BorderLayout.NORTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    @Override
    public VisualFunctionComponent getTemplateNode() {
        return (VisualFunctionComponent) super.getTemplateNode();
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create a component, then right-click on the component to add contacts.";
    }

}
