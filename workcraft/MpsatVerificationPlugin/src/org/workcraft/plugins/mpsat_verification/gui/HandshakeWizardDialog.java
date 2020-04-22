package org.workcraft.plugins.mpsat_verification.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.visual.SizeHelper;
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
import java.util.List;
import java.util.*;

public class HandshakeWizardDialog extends PresetDialog<HandshakeParameters> {

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

    private static class ListMultipleSelectionModel extends DefaultListSelectionModel {

        private boolean started = false;

        @Override
        public void setSelectionInterval(int index0, int index1) {
            if (!started) {
                if (isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                } else {
                    super.addSelectionInterval(index0, index1);
                }
            }
            started = true;
        }

        @Override
        public void setValueIsAdjusting(boolean isAdjusting) {
            if (!isAdjusting) {
                started = false;
            }
        }
    }

    class ListCellRenderer extends DefaultListCellRenderer {

        private final Color color;

        ListCellRenderer(Color color) {
            this.color = color;
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setForeground(color);
            return this;
        }
    }

    class SignalList extends JList<String> {

        SignalList(Collection<String> signals, Color color) {
            super(signals.toArray(new String[signals.size()]));
            setBorder(SizeHelper.getEmptyBorder());
            setSelectionModel(new ListMultipleSelectionModel());
            setCellRenderer(new ListCellRenderer(color));
        }
    }

    public HandshakeWizardDialog(Window owner, HandshakePresetManager presetManager) {
        super(owner, "Handshake wizard", presetManager);
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
    public JPanel createControlsPanel() {
        JPanel result = super.createControlsPanel();
        result.setLayout(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL},
                new double[]{TableLayout.PREFERRED, TableLayout.FILL}));

        result.add(createOptionsPanel(), new TableLayoutConstraints(0, 1));
        // Preset panel has to be created the last as its guiMapper refers to other controls
        presetPanel = createPresetPanel();
        result.add(presetPanel, new TableLayoutConstraints(0, 0));
        return result;
    }

    private JPanel createOptionsPanel() {
        JPanel result = new JPanel(new BorderLayout());
        result.setLayout(GuiUtils.createBorderLayout());
        result.setBorder(SizeHelper.getGapBorder());

        passiveRadioButton = new JRadioButton("passive");
        activeRadioButton = new JRadioButton("active");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(passiveRadioButton);
        buttonGroup.add(activeRadioButton);

        JPanel typePanel = new JPanel(GuiUtils.createFlowLayout());
        typePanel.add(new JLabel("Type:"));
        typePanel.add(passiveRadioButton);
        typePanel.add(activeRadioButton);

        Stg stg = getUserData().getStg();

        List<String> inputSignals = new ArrayList<>(stg.getSignalReferences(Signal.Type.INPUT));
        SortUtils.sortNatural(inputSignals);
        inputList = new SignalList(inputSignals, SignalCommonSettings.getInputColor());
        inputList.addListSelectionListener(l -> updateOkEnableness());

        List<String> outputSignals = new ArrayList<>(stg.getSignalReferences(Signal.Type.OUTPUT));
        SortUtils.sortNatural(outputSignals);
        outputList = new SignalList(outputSignals, SignalCommonSettings.getOutputColor());
        outputList.addListSelectionListener(l -> updateOkEnableness());

        JPanel signalPanel = new JPanel(GuiUtils.createGridLayout(1, 2));
        JScrollPane reqScroll = new JScrollPane();
        signalPanel.add(GuiUtils.createLabeledComponent(reqScroll, REQ_LABEL + ":", BorderLayout.NORTH));
        JScrollPane ackScroll = new JScrollPane();
        signalPanel.add(GuiUtils.createLabeledComponent(ackScroll, ACK_LABEL + ":", BorderLayout.NORTH));

        JPanel statePanel = new JPanel(GuiUtils.createFlowLayout());
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

        JPanel receptivenessPanel = new JPanel(GuiUtils.createGridLayout(2, 1));
        checkAssertEnabledCheckbox = new JCheckBox();
        checkAssertEnabledCheckbox.setSelected(true);
        checkWithdrawEnabledCheckbox = new JCheckBox();
        checkWithdrawEnabledCheckbox.setSelected(true);
        receptivenessPanel.add(checkAssertEnabledCheckbox);
        receptivenessPanel.add(checkWithdrawEnabledCheckbox);

        allowInversionCheckbox = new JCheckBox(" Allow arbitrary inversions of signals");
        receptivenessPanel.add(allowInversionCheckbox);

        JPanel optionsPanel = new JPanel(GuiUtils.createBorderLayout());
        optionsPanel.add(GuiUtils.createBorderedComponent(receptivenessPanel, "Receptiveness check: "), BorderLayout.NORTH);
        optionsPanel.add(GuiUtils.createBorderedComponent(statePanel, "Initial state: "), BorderLayout.CENTER);
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
        switch (state) {
        case REQ0ACK0:
            return LABEL_PREFIX + REQ_LABEL + ASSERTED_LABEL;
        case REQ1ACK0:
            return LABEL_PREFIX + ACK_LABEL + ASSERTED_LABEL;
        case REQ1ACK1:
            return LABEL_PREFIX + REQ_LABEL + WITHDRAWN_LABEL;
        case REQ0ACK1:
            return LABEL_PREFIX + ACK_LABEL + WITHDRAWN_LABEL;
        }
        return "";
    }


    private String getStateTooltip(HandshakeParameters.State state) {
        switch (state) {
        case REQ0ACK0:
            return TOOLTIP_PREFIX + REQ_LABEL + ASSERTED_SUFFIX;
        case REQ1ACK0:
            return TOOLTIP_PREFIX + ACK_LABEL + ASSERTED_SUFFIX;
        case REQ1ACK1:
            return TOOLTIP_PREFIX + REQ_LABEL + WITHDRAWN_SUFFIX;
        case REQ0ACK1:
            return TOOLTIP_PREFIX + ACK_LABEL + WITHDRAWN_SUFFIX;
        }
        return "";
    }

    private PresetManagerPanel<HandshakeParameters> createPresetPanel() {
        HandshakePresetManager presetManager = getUserData();

        Set<String> empty = Collections.emptySet();
        presetManager.addExample("Clear", new HandshakeParameters(empty, empty));

        DataMapper<HandshakeParameters> guiMapper = new DataMapper<HandshakeParameters>() {
            @Override
            public void applyDataToControls(HandshakeParameters data) {
                passiveRadioButton.setSelected(data.getType() == HandshakeParameters.Type.PASSIVE);
                activeRadioButton.setSelected(data.getType() == HandshakeParameters.Type.ACTIVE);
                selectSignals(getReqList(), data.getReqs());
                selectSignals(getAckList(), data.getAcks());
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
        HandshakeParameters.Type type = null;
        if (passiveRadioButton.isSelected()) {
            type = HandshakeParameters.Type.PASSIVE;
        }
        if (activeRadioButton.isSelected()) {
            type = HandshakeParameters.Type.ACTIVE;
        }

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

        return new HandshakeParameters(type, reqs, acks,
                checkAssertEnabledCheckbox.isSelected(), checkWithdrawEnabledCheckbox.isSelected(),
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
        return  first +  " must be enabled after " + second;
    }

    private String getCheckWithdrawEnabledText() {
        String first = passiveRadioButton.isSelected() ? REQ_LABEL + WITHDRAWN_LABEL : ACK_LABEL + WITHDRAWN_LABEL;
        String second = passiveRadioButton.isSelected() ? ACK_LABEL + ASSERTED_LABEL : REQ_LABEL + WITHDRAWN_LABEL;
        return  first +  " must be enabled after " + second;
    }

    private void updateOkEnableness() {
        setOkEnableness((inputList != null) && !inputList.getSelectedValuesList().isEmpty()
                && (outputList != null) && !outputList.getSelectedValuesList().isEmpty());
    }

}
