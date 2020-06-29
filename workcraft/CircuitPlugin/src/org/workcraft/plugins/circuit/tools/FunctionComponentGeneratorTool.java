package org.workcraft.plugins.circuit.tools;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.Framework;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.plugins.circuit.utils.MutexUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class FunctionComponentGeneratorTool extends NodeGeneratorTool {

    private JPanel panel = null;
    private LibraryItem libraryItem = null;

    class FilterItem {
        private final String description;
        private final List<LibraryItem> components;

        FilterItem(String description, List<LibraryItem> components) {
            this.description = description;
            this.components = components;
        }

        @Override
        public String toString() {
            return description + " (" + components.size() + ")";
        }
    }

    interface Instantiator extends BiConsumer<VisualCircuit, VisualFunctionComponent> {
        class Empty implements Instantiator {
            private Empty() {
            }

            @Override
            public void accept(VisualCircuit circuit, VisualFunctionComponent component) {
                VisualContact contact = component.createContact(IOType.OUTPUT);
                component.setPositionByDirection(contact, VisualContact.Direction.EAST, false);
            }

            public static final Empty INSTANCE = new Empty();
        }
    }

    static class LibraryItem {
        private final String name;
        private final Type type;
        private final Instantiator instantiator;
        private final String description;

        enum Type {
            UNDEFINED,
            COMBINATIONAL_GATE,
            SEQUENTIAL_GATE,
            ARBITRATION_PRIMITIVE,
        }

        LibraryItem() {
            this("", Type.UNDEFINED, Instantiator.Empty.INSTANCE, "");
        }

        LibraryItem(String name, Type type, Instantiator instantiator, String description) {
            this.name = name;
            this.type = type;
            this.instantiator = instantiator;
            this.description = description;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class SymbolPanel extends JPanel {
        private static final double MAX_SCALE_FACTOR = 50.0;

        private final VisualCircuit circuit = new VisualCircuit(new Circuit()) {
            @Override
            public void setGraphEditorTools() {
                // Prevent creation of GraphEditorTools because it leads to a loop
                // between VisualCircuit and FunctionComponentGeneratorTool classes.
            }
        };

        private VisualFunctionComponent component = null;

        public void setInstntiator(Instantiator instantiator) {
            if (component != null) {
                circuit.remove(component);
            }
            component = new VisualFunctionComponent(new FunctionComponent()) {
                @Override
                public boolean getNameVisibility() {
                    return false;
                }
                @Override
                public boolean getLabelVisibility() {
                    return false;
                }
            };
            circuit.getMathModel().add(component.getReferencedComponent());
            circuit.add(component);
            instantiator.accept(circuit, component);
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            setBackground(EditorCommonSettings.getBackgroundColor());
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(getWidth() / 2, getHeight() / 2);
            if (component != null) {
                Rectangle2D bb = component.getBoundingBox();
                double scaleX = getWidth() / bb.getWidth();
                double scaleY = getHeight() / bb.getHeight();
                double scale = Math.min(Math.min(scaleX, scaleY), MAX_SCALE_FACTOR);
                g2d.scale(scale, scale);
                circuit.draw(g2d, Decorator.Empty.INSTANCE);
            }
        }
    }

    public FunctionComponentGeneratorTool() {
        super(new DefaultNodeGenerator(FunctionComponent.class));
    }

    @Override
    public VisualNode generateNode(VisualModel model, Point2D position) throws NodeCreationException {
        VisualFunctionComponent component = (VisualFunctionComponent) super.generateNode(model, position);
        if (libraryItem != null) {
            libraryItem.instantiator.accept((VisualCircuit) model, component);
        }
        return component;
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }

        JPanel selectPanel = new JPanel(GuiUtils.createTableLayout(
                new double[]{TableLayout.PREFERRED, TableLayout.FILL},
                new double[]{TableLayout.PREFERRED, TableLayout.PREFERRED}));

        JComboBox<FilterItem> filterComboBox = new JComboBox<>();
        JComboBox<LibraryItem> libraryComboBox = new JComboBox<>();
        selectPanel.add(new JLabel("Filter:"), new TableLayoutConstraints(0, 0));
        selectPanel.add(filterComboBox, new TableLayoutConstraints(1, 0));
        selectPanel.add(new JLabel("Component:"), new TableLayoutConstraints(0, 1));
        selectPanel.add(libraryComboBox, new TableLayoutConstraints(1, 1));

        SymbolPanel symbolPanel = new SymbolPanel();
        JPanel infoPanel = new JPanel(GuiUtils.createBorderLayout());
        JLabel infoLabel = new JLabel();
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        filterComboBox.addActionListener(event -> {
            libraryComboBox.removeAllItems();
            libraryComboBox.addItem(new LibraryItem());
            Object item = filterComboBox.getSelectedItem();
            if (item instanceof FilterItem) {
                FilterItem filterItem = (FilterItem) item;
                filterItem.components.forEach(libraryComboBox::addItem);
            }
        });

        libraryComboBox.addActionListener(event -> {
            Object item = libraryComboBox.getSelectedItem();
            if (item instanceof LibraryItem) {
                libraryItem = (LibraryItem) item;
                //LibraryItem libraryItem = (LibraryItem) item;
                getTemplateNode().getReferencedComponent().setModule(libraryItem.name);
                getTemplateNode().setRenderType(libraryItem.type == LibraryItem.Type.ARBITRATION_PRIMITIVE
                        ? ComponentRenderingResult.RenderType.BOX : ComponentRenderingResult.RenderType.GATE);

                infoLabel.setText(libraryItem.description);
                symbolPanel.setInstntiator(libraryItem.instantiator);
                Framework.getInstance().updatePropertyView();
                editor.requestFocus();
            }
        });

        filterComboBox.removeAllItems();

        List<LibraryItem> allComponents = getLibraryItems();

        List<LibraryItem> combinationalGates = allComponents.stream()
                .filter(o -> o.type == LibraryItem.Type.COMBINATIONAL_GATE)
                .collect(Collectors.toList());

        List<LibraryItem> sequentialGates = allComponents.stream()
                .filter(o -> o.type == LibraryItem.Type.SEQUENTIAL_GATE)
                .collect(Collectors.toList());

        List<LibraryItem> arbitrationPrimitives = allComponents.stream()
                .filter(o -> o.type == LibraryItem.Type.ARBITRATION_PRIMITIVE)
                .collect(Collectors.toList());

        filterComboBox.addItem(new FilterItem("All available components", allComponents));

        filterComboBox.addItem(new FilterItem("Simple combinational gates", combinationalGates));

        filterComboBox.addItem(new FilterItem("Multi-level combinational gates", combinationalGates));

        filterComboBox.addItem(new FilterItem("Sequential gates", sequentialGates));

        filterComboBox.addItem(new FilterItem("Arbitration primitives", arbitrationPrimitives));

        panel = new JPanel(GuiUtils.createBorderLayout());
        panel.add(selectPanel, BorderLayout.NORTH);
        panel.add(symbolPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private List<LibraryItem> getLibraryItems() {
        List<LibraryItem> result = new ArrayList<>();
        Library library = LibraryManager.getLibrary();
        if (library != null) {
            for (String gateName : library.getNames()) {
                Gate gate = library.get(gateName);
                LibraryItem.Type type = gate.isSequential()
                        ? LibraryItem.Type.SEQUENTIAL_GATE
                        : LibraryItem.Type.COMBINATIONAL_GATE;

                Instantiator instantiator = (circuit, component)
                        -> GenlibUtils.instantiateGate(gate, circuit, component);

                String description = gate.function.name + " = " + gate.function.formula;
                result.add(new LibraryItem(gateName, type, instantiator, description));
            }
        }
        result.add(createMutexItem());
        Collections.sort(result, Comparator.comparing(LibraryItem::toString));
        return result;
    }

    private LibraryItem createMutexItem() {
        Mutex module = CircuitSettings.parseMutexData();
        String r1Name = module.r1.name;
        String g1Name = module.g1.name;
        String r2Name = module.r2.name;
        String g2Name = module.g2.name;
        String g1Set = MutexUtils.getGrantSet(r1Name, g2Name, r2Name);
        String g1Reset = MutexUtils.getGrantReset(r1Name);
        String g2Set = MutexUtils.getGrantSet(r2Name, g1Name, r1Name);
        String g2Reset = MutexUtils.getGrantReset(r2Name);
        String description = "<html>" + MutexUtils.getGrantSetReset(g1Name, g1Set, g1Reset)
                + "<br>" + MutexUtils.getGrantSetReset(g2Name, g2Set, g2Reset) + "</html>";

        Instantiator instantiator = (circuit, component) -> {
            component.getReferencedComponent().setModule(module.name);

            VisualFunctionContact r1Contact = component.createContact(IOType.INPUT);
            circuit.setMathName(r1Contact, r1Name);
            r1Contact.setPosition(new Point2D.Double(-1.5, -1.0));

            VisualFunctionContact g1Contact = component.createContact(IOType.OUTPUT);
            circuit.setMathName(g1Contact, g1Name);
            g1Contact.setPosition(new Point2D.Double(1.5, -1.0));

            VisualFunctionContact r2Contact = component.createContact(IOType.INPUT);
            circuit.setMathName(r2Contact, r2Name);
            r2Contact.setPosition(new Point2D.Double(-1.5, 1.0));

            VisualFunctionContact g2Contact = component.createContact(IOType.OUTPUT);
            circuit.setMathName(g2Contact, g2Name);
            g2Contact.setPosition(new Point2D.Double(1.5, 1.0));

            MutexUtils.setMutexFunctions(circuit, component, g1Contact, g1Set, g1Reset);
            MutexUtils.setMutexFunctions(circuit, component, g2Contact, g2Set, g2Reset);
        };
        return new LibraryItem(module.name, LibraryItem.Type.ARBITRATION_PRIMITIVE, instantiator, description);
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
