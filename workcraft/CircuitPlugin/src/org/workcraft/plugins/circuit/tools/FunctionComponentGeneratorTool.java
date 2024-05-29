package org.workcraft.plugins.circuit.tools;

import org.workcraft.Framework;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.controls.IntRangeSlider;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.Library;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.plugins.circuit.utils.ArbitrationUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Wait;
import org.workcraft.types.Pair;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.SortUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class FunctionComponentGeneratorTool extends NodeGeneratorTool {

    private final JRadioButton allTypeFilter = new JRadioButton("all");
    private final JRadioButton combTypeFilter = new JRadioButton("comb");
    private final JRadioButton seqTypeFilter = new JRadioButton("seq");
    private final JRadioButton arbTypeFilter = new JRadioButton("arb");
    private final IntRangeSlider pinsFilter = new IntRangeSlider();
    private final JTextField nameFilter = new JTextField("");
    private final JScrollPane libraryScroll = new JScrollPane();
    private final InstancePanel instancePanel = new InstancePanel();

    private JPanel panel = null;
    private Library library = null;
    private List<LibraryItem> libraryItems = null;
    private LibraryItem libraryItem = null;

    static class LibraryItem {

        private final String name;
        private final Type type;
        private final int pinCount;
        private final InstancePanel.Instantiator instantiator;

        enum Type {
            UNDEFINED,
            COMBINATIONAL_GATE,
            SEQUENTIAL_GATE,
            ARBITRATION_PRIMITIVE,
        }

        LibraryItem(String name, Type type, int pinCount, InstancePanel.Instantiator instantiator) {
            this.name = name;
            this.type = type;
            this.pinCount = pinCount;
            this.instantiator = instantiator;
        }

        @Override
        public String toString() {
            return name.isEmpty() ? "\u00BB Custom component" : name;
        }
    }

    static class LibraryList extends JList<LibraryItem> {

        LibraryList(List<LibraryItem> items) {
            super(new Vector<>(items));
            setBorder(GuiUtils.getEmptyBorder());
        }
    }

    public FunctionComponentGeneratorTool() {
        super(new DefaultNodeGenerator(FunctionComponent.class));
    }

    @Override
    public VisualFunctionComponent generateNode(VisualModel model, Point2D position) {
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

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, libraryScroll, instancePanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(filterPanel.getMinimumSize().height);
        splitPane.setResizeWeight(0.5);

        VisualFunctionComponent templateNode = getTemplateNode();
        templateNode.addObserver((StateObserver) e -> instancePanel.repaint());
        instancePanel.setTemplateNode(templateNode);

        panel = new JPanel(GuiUtils.createBorderLayout());
        panel.setBorder(GuiUtils.getGapBorder());
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
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

        allTypeFilter.setToolTipText("All components");
        combTypeFilter.setToolTipText("Combinational gates");
        seqTypeFilter.setToolTipText("Sequential elements");
        arbTypeFilter.setToolTipText("Arbitration primitives");


        allTypeFilter.addActionListener(event -> updateLibraryList(editor));
        combTypeFilter.addActionListener(event -> updateLibraryList(editor));
        seqTypeFilter.addActionListener(event -> updateLibraryList(editor));
        arbTypeFilter.addActionListener(event -> updateLibraryList(editor));
        allTypeFilter.setSelected(true);
        return result;
    }

    private JPanel createPinsFilterPanel(final GraphEditor editor) {
        Pair<Integer, Integer> pinRange = calcPinRange();
        pinsFilter.setMinimum(pinRange.getFirst());
        pinsFilter.setMaximum(pinRange.getSecond());
        pinsFilter.addChangeListener(event -> updateLibraryList(editor));
        return GuiUtils.createLabeledComponent(pinsFilter, "Pins:  ");
    }

    private Pair<Integer, Integer> calcPinRange() {
        List<LibraryItem> libraryItems = getLibraryItems();
        int min = 0;
        int max = 0;
        for (LibraryItem libraryItem : libraryItems) {
            if ((min == 0) || (libraryItem.pinCount < min)) {
                min = libraryItem.pinCount;
            }
            if ((min == 0) || (libraryItem.pinCount > max)) {
                max = libraryItem.pinCount;
            }
        }
        return Pair.of(min, max);
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
        components.add(createCustomItem());
        components.addAll(getLibraryItems().stream()
                .filter(o -> filterByType(o) && filterByPins(o) && filterByName(o))
                .toList());

        LibraryList libraryList = new LibraryList(components);
        libraryScroll.setViewportView(libraryList);
        libraryList.addListSelectionListener(e -> {
            libraryItem = libraryList.getSelectedValue();
            if (libraryItem != null) {
                getTemplateNode().getReferencedComponent().setModule(this.libraryItem.name);
                instancePanel.setInstantiator(this.libraryItem.instantiator);
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
        return (libraryItem.pinCount >= pinsFilter.getValue())
                && (libraryItem.pinCount <= pinsFilter.getSecondValue());
    }

    private boolean filterByName(LibraryItem libraryItem) {
        String needle = nameFilter.getText().trim().toLowerCase(Locale.ROOT);
        String haystack = libraryItem.name.toLowerCase(Locale.ROOT);
        return haystack.contains(needle);
    }

    private List<LibraryItem> getLibraryItems() {
        Library currentlibrary = LibraryManager.getLibrary();
        if ((currentlibrary == library) && (libraryItems != null)) {
            return libraryItems;
        }

        library = currentlibrary;
        libraryItems = new ArrayList<>();
        if (library != null) {
            for (String gateName : library.getNames()) {
                Gate gate = library.get(gateName);
                LibraryItem.Type type = gate.isSequential()
                        ? LibraryItem.Type.SEQUENTIAL_GATE
                        : LibraryItem.Type.COMBINATIONAL_GATE;

                int pinCount = GenlibUtils.getPinCount(gate);

                InstancePanel.Instantiator instantiator = (circuit, component) ->
                        GenlibUtils.instantiateGate(gate, circuit, component);

                libraryItems.add(new LibraryItem(gateName, type, pinCount, instantiator));
            }
        }

        LibraryItem wait1Item = createWaitItem(Wait.Type.WAIT1);
        if (wait1Item != null) {
            libraryItems.add(wait1Item);
        }

        LibraryItem wait0Item = createWaitItem(Wait.Type.WAIT0);
        if (wait0Item != null) {
            libraryItems.add(wait0Item);
        }

        LibraryItem mutexLateItem = createMutexItem(Mutex.Protocol.LATE);
        if (mutexLateItem != null) {
            libraryItems.add(mutexLateItem);
        }

        LibraryItem mutexEarlyItem = createMutexItem(Mutex.Protocol.EARLY);
        if (mutexEarlyItem != null) {
            libraryItems.add(mutexEarlyItem);
        }

        libraryItems.sort((item1, item2) -> SortUtils.compareNatural(item1.toString(), item2.toString()));
        return libraryItems;
    }

    private LibraryItem createCustomItem() {
        InstancePanel.Instantiator instantiator = (circuit, component) -> {
            VisualContact contact = component.createContact(Contact.IOType.OUTPUT);
            component.setPositionByDirection(contact, VisualContact.Direction.EAST, false);
        };
        return new LibraryItem("", LibraryItem.Type.UNDEFINED, 0, instantiator);
    }

    private LibraryItem createWaitItem(Wait.Type type) {
        Wait module = CircuitSettings.parseWaitData(type);
        if (module == null) {
            return null;
        }
        InstancePanel.Instantiator instantiator = (circuit, component) -> {
            component.setRenderType(ComponentRenderingResult.RenderType.GATE);
            component.getReferencedComponent().setIsArbitrationPrimitive(true);

            VisualFunctionContact sigContact = component.createContact(IOType.INPUT);
            circuit.setMathName(sigContact, module.sig.name);
            sigContact.setPosition(new Point2D.Double(-1.5, 0.0));

            VisualFunctionContact ctrlContact = component.createContact(IOType.INPUT);
            circuit.setMathName(ctrlContact, module.ctrl.name);
            ctrlContact.setDirection(VisualContact.Direction.EAST);
            ctrlContact.setPosition(new Point2D.Double(1.5, 0.5));

            VisualFunctionContact sanContact = component.createContact(IOType.OUTPUT);
            circuit.setMathName(sanContact, module.san.name);
            sanContact.setPosition(new Point2D.Double(1.5, -0.5));

            ArbitrationUtils.assignWaitFunctions(type, sigContact, ctrlContact, sanContact);
        };
        return new LibraryItem(module.name, LibraryItem.Type.ARBITRATION_PRIMITIVE, 3, instantiator);
    }

    private LibraryItem createMutexItem(Mutex.Protocol protocol) {
        Mutex module = CircuitSettings.parseMutexData();
        if (module == null) {
            return null;
        }
        InstancePanel.Instantiator instantiator = (circuit, component) -> {
            component.setRenderType(ComponentRenderingResult.RenderType.GATE);
            component.getReferencedComponent().setIsArbitrationPrimitive(true);

            VisualFunctionContact r1Contact = component.createContact(IOType.INPUT);
            circuit.setMathName(r1Contact, module.r1.name);
            r1Contact.setPosition(new Point2D.Double(-1.5, -0.5));

            VisualFunctionContact g1Contact = component.createContact(IOType.OUTPUT);
            circuit.setMathName(g1Contact, module.g1.name);
            g1Contact.setPosition(new Point2D.Double(1.5, -0.5));

            VisualFunctionContact r2Contact = component.createContact(IOType.INPUT);
            circuit.setMathName(r2Contact, module.r2.name);
            r2Contact.setPosition(new Point2D.Double(-1.5, 0.5));

            VisualFunctionContact g2Contact = component.createContact(IOType.OUTPUT);
            circuit.setMathName(g2Contact, module.g2.name);
            g2Contact.setPosition(new Point2D.Double(1.5, 0.5));

            ArbitrationUtils.assignMutexFunctions(protocol, r1Contact, g1Contact, r2Contact, g2Contact);
        };
        String moduleName = ArbitrationUtils.appendMutexProtocolSuffix(module.name, protocol);
        return new LibraryItem(moduleName, LibraryItem.Type.ARBITRATION_PRIMITIVE, 4, instantiator);
    }

    @Override
    public VisualFunctionComponent getTemplateNode() {
        return (VisualFunctionComponent) super.getTemplateNode();
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create a component, then right-click on the component to add contacts.";
    }

    @Override
    public void activated(GraphEditor editor) {
        super.activated(editor);
        updateLibraryList(editor);
    }

}
