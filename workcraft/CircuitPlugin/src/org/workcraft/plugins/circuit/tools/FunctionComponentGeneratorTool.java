package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.gui.controls.IntRangeSlider;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.observation.StateObserver;
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
import org.workcraft.plugins.stg.Wait;
import org.workcraft.types.Pair;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class FunctionComponentGeneratorTool extends NodeGeneratorTool {

    private final JRadioButton allTypeFilter = new JRadioButton("all");
    private final JRadioButton combTypeFilter = new JRadioButton("com");
    private final JRadioButton seqTypeFilter = new JRadioButton("seq");
    private final JRadioButton arbTypeFilter = new JRadioButton("arb");
    private final IntRangeSlider pinsFilter = new IntRangeSlider();
    private final JTextField nameFilter = new JTextField("");
    private final JScrollPane libraryScroll = new JScrollPane();
    private final SymbolPanel symbolPanel = new SymbolPanel();
    private final JLabel infoLabel = new JLabel();

    private JPanel panel = null;
    private List<LibraryItem> libraryItems = null;
    private LibraryItem libraryItem = null;

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
        private int pinCount;
        private final Instantiator instantiator;
        private final String description;

        enum Type {
            UNDEFINED,
            COMBINATIONAL_GATE,
            SEQUENTIAL_GATE,
            ARBITRATION_PRIMITIVE,
        }

        LibraryItem() {
            this("", Type.UNDEFINED, 0, Instantiator.Empty.INSTANCE, "");
        }

        LibraryItem(String name, Type type, int pinCount, Instantiator instantiator, String description) {
            this.name = name;
            this.type = type;
            this.pinCount = pinCount;
            this.instantiator = instantiator;
            this.description = description;
        }

        @Override
        public String toString() {
            return name.isEmpty() ? "\u00BB Custom component" : name;
        }
    }

    class LibraryList extends JList<LibraryItem> {

        LibraryList(List<LibraryItem> items) {
            super(new Vector<>(items));
            setBorder(GuiUtils.getEmptyBorder());
        }
    }

    class SymbolPanel extends JPanel {
        private static final double MAX_SCALE_FACTOR = 50.0;

        private final VisualCircuit circuit = new VisualCircuit(new Circuit()) {
            @Override
            public void registerGraphEditorTools() {
                // Prevent creation registration of GraphEditorTools because it leads to a
                // loop between VisualCircuit and FunctionComponentGeneratorTool classes.
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
            };
            circuit.getMathModel().add(component.getReferencedComponent());
            circuit.add(component);
            instantiator.accept(circuit, component);
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(EditorCommonSettings.getBackgroundColor());
            if (component != null) {
                component.copyStyle(getTemplateNode());
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                if (component.getRenderType() == ComponentRenderingResult.RenderType.BOX) {
                    width *= 0.8;
                }
                int height = getHeight();
                g2d.translate(width / 2, height / 2);
                Rectangle2D bb = component.getBoundingBox();
                double scaleX = (width - 2 * SizeHelper.getLayoutHGap()) / bb.getWidth();
                double scaleY = (height - 2 * SizeHelper.getLayoutVGap()) / bb.getHeight();
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

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.add(createTypeFilterPanel(editor));
        filterPanel.add(GuiUtils.createVGap());
        filterPanel.add(createPinsFilterPanel(editor));
        filterPanel.add(GuiUtils.createVGap());
        filterPanel.add(createNameFilterPanel(editor));

        JPanel selectPanel = new JPanel(GuiUtils.createBorderLayout());
        selectPanel.add(filterPanel, BorderLayout.NORTH);
        selectPanel.add(libraryScroll, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(GuiUtils.createBorderLayout());
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        getTemplateNode().addObserver((StateObserver) e -> symbolPanel.repaint());

        panel = new JPanel(GuiUtils.createBorderLayout());
        panel.setBorder(GuiUtils.getGapBorder());
        panel.add(selectPanel, BorderLayout.NORTH);
        panel.add(symbolPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(0, 0));
        updateLibraryList(editor);
        return panel;
    }

    private JPanel createTypeFilterPanel(final GraphEditor editor) {
        JPanel result = new JPanel(GuiUtils.createNogapFlowLayout());
        result.add(new JLabel("Type: "));
        result.add(allTypeFilter);
        result.add(combTypeFilter);
        result.add(seqTypeFilter);
        result.add(arbTypeFilter);

        ButtonGroup typeFilterGroup = new ButtonGroup();
        typeFilterGroup.add(allTypeFilter);
        typeFilterGroup.add(combTypeFilter);
        typeFilterGroup.add(seqTypeFilter);
        typeFilterGroup.add(arbTypeFilter);

        allTypeFilter.addActionListener(event -> updateLibraryList(editor));
        combTypeFilter.addActionListener(event -> updateLibraryList(editor));
        seqTypeFilter.addActionListener(event -> updateLibraryList(editor));
        arbTypeFilter.addActionListener(event -> updateLibraryList(editor));
        allTypeFilter.setSelected(true);
        return result;
    }

    private JPanel createPinsFilterPanel(final GraphEditor editor) {
        Pair<Integer, Integer> pinRange = GenlibUtils.getPinRange(LibraryManager.getLibrary());
        pinsFilter.setMinimum(pinRange.getFirst());
        pinsFilter.setMaximum(pinRange.getSecond());
        pinsFilter.addChangeListener(event -> updateLibraryList(editor));
        return GuiUtils.createLabeledComponent(pinsFilter, "Pins:  ");
    }

    private JPanel createNameFilterPanel(final GraphEditor editor) {
        nameFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent event) {
                updateLibraryList(null);
            }
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateLibraryList(null);
            }
            @Override
            public void changedUpdate(DocumentEvent event) {
                updateLibraryList(null);
            }
        });
        nameFilter.setText("");

        nameFilter.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
                editor.requestFocus();
            }
        });

        nameFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent event) {
                char keyChar = event.getKeyChar();
                if ((keyChar == KeyEvent.VK_ENTER) || (keyChar == KeyEvent.VK_ESCAPE)) {
                    editor.requestFocus();
                }
            }
        });

        return GuiUtils.createLabeledComponent(nameFilter, "Name:");
    }

    private void updateLibraryList(final GraphEditor editor) {
        ArrayList<LibraryItem> components = new ArrayList<>();
        components.add(new LibraryItem());
        components.addAll(getLibraryItems().stream()
                .filter(o -> filterByType(o) && filterByPins(o) && filterByName(o))
                .collect(Collectors.toList()));

        LibraryList libraryList = new LibraryList(components);
        libraryScroll.setViewportView(libraryList);
        libraryList.addListSelectionListener(e -> {
            libraryItem = libraryList.getSelectedValue();
            if (libraryItem != null) {
                getTemplateNode().getReferencedComponent().setModule(this.libraryItem.name);
                infoLabel.setText(this.libraryItem.description);
                symbolPanel.setInstntiator(this.libraryItem.instantiator);
                Framework.getInstance().updatePropertyView();
            }
        });
        libraryList.setSelectedIndex(0);
        if (editor != null) {
            editor.requestFocus();
        }
    }

    private boolean filterByType(LibraryItem libraryItem) {
        if (combTypeFilter.isSelected()) {
            return libraryItem.type == LibraryItem.Type.COMBINATIONAL_GATE;
        }
        if (seqTypeFilter.isSelected()) {
            return libraryItem.type == LibraryItem.Type.SEQUENTIAL_GATE;
        }
        if (arbTypeFilter.isSelected()) {
            return libraryItem.type == LibraryItem.Type.ARBITRATION_PRIMITIVE;
        }
        return true;
    }

    private boolean filterByPins(LibraryItem libraryItem) {
        return (libraryItem.pinCount >= pinsFilter.getValue()) && (libraryItem.pinCount <= pinsFilter.getSecondValue());
    }

    private boolean filterByName(LibraryItem libraryItem) {
        String needle = nameFilter.getText().trim().toLowerCase(Locale.ROOT);
        String haystack = libraryItem.name.toLowerCase(Locale.ROOT);
        return haystack.contains(needle);
    }

    private List<LibraryItem> getLibraryItems() {
        if (libraryItems != null) {
            return libraryItems;
        }

        libraryItems = new ArrayList<>();
        Library library = LibraryManager.getLibrary();
        if (library != null) {
            for (String gateName : library.getNames()) {
                Gate gate = library.get(gateName);
                LibraryItem.Type type = gate.isSequential()
                        ? LibraryItem.Type.SEQUENTIAL_GATE
                        : LibraryItem.Type.COMBINATIONAL_GATE;

                int pinCount = GenlibUtils.getPinCount(gate);

                Instantiator instantiator = (circuit, component)
                        -> GenlibUtils.instantiateGate(gate, circuit, component);

                String description = gate.function.name + " = " + gate.function.formula;
                libraryItems.add(new LibraryItem(gateName, type, pinCount, instantiator, description));
            }
        }
        libraryItems.add(createWaitItem());
        libraryItems.add(createMutexItem());
        Collections.sort(libraryItems, Comparator.comparing(LibraryItem::toString));
        return libraryItems;
    }

    private LibraryItem createWaitItem() {
        Wait module = CircuitSettings.parseWaitData();
        String name = module.name;
        String sigName = module.sig.name;
        String ctrlName = module.ctrl.name;
        String sanName = module.san.name;
        String description = sanName + " = " + ctrlName;

        Instantiator instantiator = (circuit, component) -> {
            component.setRenderType(ComponentRenderingResult.RenderType.BOX);

            VisualFunctionContact sigContact = component.createContact(IOType.INPUT);
            circuit.setMathName(sigContact, sigName);
            sigContact.setPosition(new Point2D.Double(-1.5, 0.0));

            VisualFunctionContact ctrlContact = component.createContact(IOType.INPUT);
            circuit.setMathName(ctrlContact, ctrlName);
            ctrlContact.setDirection(VisualContact.Direction.EAST);
            ctrlContact.setPosition(new Point2D.Double(1.5, 0.5));

            VisualFunctionContact sanContact = component.createContact(IOType.OUTPUT);
            circuit.setMathName(sanContact, sanName);
            sanContact.setPosition(new Point2D.Double(1.5, -0.5));

            BooleanFormula setFormula = ctrlContact.getReferencedComponent();
            sanContact.getReferencedComponent().setSetFunctionQuiet(setFormula);
        };
        return new LibraryItem(name, LibraryItem.Type.ARBITRATION_PRIMITIVE, 3, instantiator, description);
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
            component.setRenderType(ComponentRenderingResult.RenderType.BOX);

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
        return new LibraryItem(module.name, LibraryItem.Type.ARBITRATION_PRIMITIVE, 4, instantiator, description);
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
