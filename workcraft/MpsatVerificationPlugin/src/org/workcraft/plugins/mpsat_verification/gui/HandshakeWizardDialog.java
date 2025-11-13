package org.workcraft.plugins.mpsat_verification.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.gui.lists.ColorListCellRenderer;
import org.workcraft.gui.lists.MultipleListSelectionModel;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat_verification.presets.HandshakeParameters;
import org.workcraft.plugins.mpsat_verification.presets.HandshakePresetManager;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.presets.DataMapper;
import org.workcraft.presets.PresetDialog;
import org.workcraft.presets.PresetManagerPanel;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.SortUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class HandshakeWizardDialog extends PresetDialog<HandshakeParameters> {

    public static final TableLayout TABLE_LAYOUT = GuiUtils.createTableLayout(
            new double[]{TableLayout.FILL},
            new double[]{TableLayout.PREFERRED, TableLayout.FILL});

    private static final String REQ_LABEL = "REQ";
    private static final String ACK_LABEL = "ACK";
    private static final String ASSERTED_LABEL = Character.toString((char) 0x2191);
    private static final String WITHDRAWN_LABEL = Character.toString((char) 0x2193);
    private static final String LABEL_PREFIX = "  ";
    private static final String TOOLTIP_PREFIX = "Before ";
    private static final String ASSERTED_SUFFIX = " asserted";
    private static final String WITHDRAWN_SUFFIX = " withdrawn";

    private PresetManagerPanel<HandshakeParameters> presetPanel;
    private JRadioButton passiveRadioButton;
    private JRadioButton activeRadioButton;
    private SignalList inputList;
    private SignalList outputList;
    private JRadioButton stateReq0Ack0Radio;
    private JRadioButton stateReq1Ack0Radio;
    private JRadioButton stateReq1Ack1Radio;
    private JRadioButton stateReq0Ack1Radio;
    private JCheckBox checkAssertEnabledCheckbox;
    private JCheckBox checkWithdrawEnabledCheckbox;
    private JCheckBox allowInversionCheckbox;

    static class SignalList extends JList<String> {

        SignalList(Collection<String> signals, Color color) {
            super(SortUtils.getSortedNatural(signals).toArray(new String[0]));
            setBorder(GuiUtils.getEmptyBorder());
            setSelectionModel(new MultipleListSelectionModel());
            setCellRenderer(new ColorListCellRenderer(item -> color));
        }
    }

    public HandshakeWizardDialog(Window owner, HandshakePresetManager presetManager) {
        super(owner, "Handshake wizard", presetManager);

        // Preset panel is set here, as it is created in overloaded createPresetPanel called from super constructor
        presetPanel.selectFirst();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                updateOkEnableness();
                requestFocus();
            }
        });
    }

    @Override
    public HandshakePresetManager getUserData() {
        return (HandshakePresetManager) super.getUserData();
    }

    @Override
    public JPanel createContentPanel() {
        JPanel result = super.createContentPanel();
        result.setLayout(TABLE_LAYOUT);
        result.add(createOptionsPanel(), new TableLayoutConstraints(0, 1));
        // Preset panel has to be created the last as its guiMapper refers to other controls
        presetPanel = createPresetPanel();
        result.add(presetPanel, new TableLayoutConstraints(0, 0));
        return result;
    }

    private JPanel createOptionsPanel() {
        JPanel result = new JPanel(GuiUtils.createBorderLayout());

        passiveRadioButton = new JRadioButton("passive");
        activeRadioButton = new JRadioButton("active");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(passiveRadioButton);
        buttonGroup.add(activeRadioButton);

        JPanel typePanel = new JPanel(GuiUtils.createNogapFlowLayout());
        typePanel.add(new JLabel("Type:"));
        typePanel.add(GuiUtils.createHGap());
        typePanel.add(passiveRadioButton);
        typePanel.add(GuiUtils.createHGap());
        typePanel.add(activeRadioButton);

        Stg stg = getUserData().getStg();

        List<String> inputSignals = SortUtils.getSortedNatural(stg.getSignalReferences(Signal.Type.INPUT));
        inputList = new SignalList(inputSignals, SignalCommonSettings.getInputColor());
        inputList.addListSelectionListener(l -> updateOkEnableness());

        List<String> outputSignals = SortUtils.getSortedNatural(stg.getSignalReferences(Signal.Type.OUTPUT));
        outputList = new SignalList(outputSignals, SignalCommonSettings.getOutputColor());
        outputList.addListSelectionListener(l -> updateOkEnableness());

        JPanel signalPanel = new JPanel(GuiUtils.createNogapGridLayout(1, 2));
        JScrollPane reqScroll = new JScrollPane();
        signalPanel.add(GuiUtils.createLabeledComponent(reqScroll, REQ_LABEL + ":", BorderLayout.NORTH));
        JScrollPane ackScroll = new JScrollPane();
        signalPanel.add(GuiUtils.createLabeledComponent(ackScroll, ACK_LABEL + ":", BorderLayout.NORTH));

        JPanel statePanel = new JPanel(GuiUtils.createNogapFlowLayout());
        statePanel.setBorder(GuiUtils.getTitledBorder("Initial state"));
        ButtonGroup stateButtonGroup = new ButtonGroup();

        stateReq0Ack0Radio = new JRadioButton(getStateLabel(HandshakeParameters.State.REQ0ACK0));
        stateReq0Ack0Radio.setToolTipText(getStateTooltip(HandshakeParameters.State.REQ0ACK0));
        stateButtonGroup.add(stateReq0Ack0Radio);
        statePanel.add(stateReq0Ack0Radio);

        stateReq1Ack0Radio = new JRadioButton(getStateLabel(HandshakeParameters.State.REQ1ACK0));
        stateReq1Ack0Radio.setToolTipText(getStateTooltip(HandshakeParameters.State.REQ1ACK0));
        stateButtonGroup.add(stateReq1Ack0Radio);
        statePanel.add(stateReq1Ack0Radio);

        stateReq1Ack1Radio = new JRadioButton(getStateLabel(HandshakeParameters.State.REQ1ACK1));
        stateReq1Ack1Radio.setToolTipText(getStateTooltip(HandshakeParameters.State.REQ1ACK1));
        stateButtonGroup.add(stateReq1Ack1Radio);
        statePanel.add(stateReq1Ack1Radio);

        stateReq0Ack1Radio = new JRadioButton(getStateLabel(HandshakeParameters.State.REQ0ACK1));
        stateReq0Ack1Radio.setToolTipText(getStateTooltip(HandshakeParameters.State.REQ0ACK1));
        stateButtonGroup.add(stateReq0Ack1Radio);
        statePanel.add(stateReq0Ack1Radio);

        stateReq0Ack0Radio.setSelected(true);

        JPanel receptivenessPanel = new JPanel(GuiUtils.createNogapGridLayout(2, 1));
        receptivenessPanel.setBorder(GuiUtils.getTitledBorder("Receptiveness check"));
        checkAssertEnabledCheckbox = new JCheckBox();
        checkAssertEnabledCheckbox.setSelected(true);
        checkWithdrawEnabledCheckbox = new JCheckBox();
        checkWithdrawEnabledCheckbox.setSelected(true);
        receptivenessPanel.add(checkAssertEnabledCheckbox, BorderLayout.NORTH);
        receptivenessPanel.add(checkWithdrawEnabledCheckbox, BorderLayout.SOUTH);

        allowInversionCheckbox = new JCheckBox(" Allow arbitrary inversions of signals");

        JPanel optionsPanel = new JPanel(GuiUtils.createBorderLayout());
        optionsPanel.add(receptivenessPanel, BorderLayout.NORTH);
        optionsPanel.add(statePanel, BorderLayout.CENTER);
        optionsPanel.add(allowInversionCheckbox, BorderLayout.SOUTH);

        passiveRadioButton.addChangeListener(l -> {
            reqScroll.setViewportView(getReqList());
            ackScroll.setViewportView(getAckList());
            checkAssertEnabledCheckbox.setText(getCheckAssertEnabledText());
            checkWithdrawEnabledCheckbox.setText(getCheckWithdrawEnabledText());
        });
        passiveRadioButton.setSelected(true);

        result.add(typePanel, BorderLayout.NORTH);
        result.add(signalPanel, BorderLayout.CENTER);
        result.add(optionsPanel, BorderLayout.SOUTH);

        return result;
    }

    private String getStateLabel(HandshakeParameters.State state) {
        return switch (state) {
            case REQ0ACK0 -> LABEL_PREFIX + REQ_LABEL + ASSERTED_LABEL;
            case REQ1ACK0 -> LABEL_PREFIX + ACK_LABEL + ASSERTED_LABEL;
            case REQ1ACK1 -> LABEL_PREFIX + REQ_LABEL + WITHDRAWN_LABEL;
            case REQ0ACK1 -> LABEL_PREFIX + ACK_LABEL + WITHDRAWN_LABEL;
        };
    }

    private String getStateTooltip(HandshakeParameters.State state) {
        return switch (state) {
            case REQ0ACK0 -> TOOLTIP_PREFIX + REQ_LABEL + ASSERTED_SUFFIX;
            case REQ1ACK0 -> TOOLTIP_PREFIX + ACK_LABEL + ASSERTED_SUFFIX;
            case REQ1ACK1 -> TOOLTIP_PREFIX + REQ_LABEL + WITHDRAWN_SUFFIX;
            case REQ0ACK1 -> TOOLTIP_PREFIX + ACK_LABEL + WITHDRAWN_SUFFIX;
        };
    }

    private PresetManagerPanel<HandshakeParameters> createPresetPanel() {
        HandshakePresetManager presetManager = getUserData();
        presetManager.addExamplePreset("Clear", new HandshakeParameters());
        DataMapper<HandshakeParameters> guiMapper = new DataMapper<>() {
            @Override
            public void applyDataToControls(HandshakeParameters data) {
                Stg stg = getUserData().getStg();
                Set<String> inputSignals = stg.getSignalReferences(Signal.Type.INPUT);
                Set<String> outputSignals = stg.getSignalReferences(Signal.Type.OUTPUT);
                Set<String> reqs = data.getReqs();
                Set<String> acks = data.getAcks();
                boolean isActive = outputSignals.containsAll(reqs) && inputSignals.containsAll(acks);
                if (isActive) {
                    activeRadioButton.setSelected(true);
                } else {
                    passiveRadioButton.setSelected(true);
                }
                selectSignals(getReqList(), reqs);
                selectSignals(getAckList(), acks);
                checkAssertEnabledCheckbox.setSelected(data.isCheckAssertion());
                checkWithdrawEnabledCheckbox.setSelected(data.isCheckWithdrawal());
                stateReq0Ack0Radio.setSelected(data.getState() == HandshakeParameters.State.REQ0ACK0);
                stateReq1Ack0Radio.setSelected(data.getState() == HandshakeParameters.State.REQ1ACK0);
                stateReq1Ack1Radio.setSelected(data.getState() == HandshakeParameters.State.REQ1ACK1);
                stateReq0Ack1Radio.setSelected(data.getState() == HandshakeParameters.State.REQ0ACK1);
                allowInversionCheckbox.setSelected(data.isAllowInversion());
            }

            @Override
            public HandshakeParameters getDataFromControls() {
                return getPresetData();
            }
        };

        return new PresetManagerPanel<>(presetManager, guiMapper);
    }

    private void selectSignals(SignalList signalList, Set<String> signals) {
        ListModel<String> signalListModel = signalList.getModel();
        List<Integer> indices = new ArrayList<>();
        for (int index = 0; index < signalListModel.getSize(); index++) {
            String signal = signalListModel.getElementAt(index);
            if (signals.contains(signal)) {
                indices.add(index);
            }
        }
        // Convert ArrayList<Integer> to int[]
        int[] itemsToSelect = indices.stream().mapToInt(i -> i).toArray();
        signalList.setSelectedIndices(itemsToSelect);
    }

    @Override
    public HandshakeParameters getPresetData() {
        Collection<String> reqs = getReqList().getSelectedValuesList();
        Collection<String> acks = getAckList().getSelectedValuesList();

        HandshakeParameters.State state = null;
        if (stateReq0Ack0Radio.isSelected()) {
            state = HandshakeParameters.State.REQ0ACK0;
        }
        if (stateReq1Ack0Radio.isSelected()) {
            state = HandshakeParameters.State.REQ1ACK0;
        }
        if (stateReq1Ack1Radio.isSelected()) {
            state = HandshakeParameters.State.REQ1ACK1;
        }
        if (stateReq0Ack1Radio.isSelected()) {
            state = HandshakeParameters.State.REQ0ACK1;
        }

        return new HandshakeParameters(reqs, acks,
                checkAssertEnabledCheckbox.isSelected(),
                checkWithdrawEnabledCheckbox.isSelected(),
                state, allowInversionCheckbox.isSelected());
    }

    private SignalList getReqList() {
        return passiveRadioButton.isSelected() ? inputList : outputList;
    }

    private SignalList getAckList() {
        return passiveRadioButton.isSelected() ? outputList : inputList;
    }

    private String getCheckAssertEnabledText() {
        String first = passiveRadioButton.isSelected() ? REQ_LABEL + ASSERTED_LABEL : ACK_LABEL + ASSERTED_LABEL;
        String second = passiveRadioButton.isSelected() ? ACK_LABEL + WITHDRAWN_LABEL : REQ_LABEL + ASSERTED_LABEL;
        return first +  " must be enabled after " + second;
    }

    private String getCheckWithdrawEnabledText() {
        String first = passiveRadioButton.isSelected() ? REQ_LABEL + WITHDRAWN_LABEL : ACK_LABEL + WITHDRAWN_LABEL;
        String second = passiveRadioButton.isSelected() ? ACK_LABEL + ASSERTED_LABEL : REQ_LABEL + WITHDRAWN_LABEL;
        return first +  " must be enabled after " + second;
    }

    private void updateOkEnableness() {
        setOkEnableness((inputList != null) && !inputList.getSelectedValuesList().isEmpty()
                && (outputList != null) && !outputList.getSelectedValuesList().isEmpty());
    }

}
