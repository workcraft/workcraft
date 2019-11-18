package org.workcraft.plugins.mpsat.gui;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HandshakeDialog extends ModalDialog<Stg> {

    private static final String REQ_NAMES_REPLACEMENT =
            "/* insert request signal names here */"; // For example: "req1", "req2"

    private static final String ACK_NAMES_REPLACEMENT =
            "/* insert acknowledgement signal names here */"; // For example: "ack1", "ack2"

    private static final String REQ_INITIALLY_ASSERTED_REPLACEMENT =
            "/* insert request initial assertion state here */"; // true or false

    private static final String ACK_INITIALLY_ASSERTED_REPLACEMENT =
            "/* insert acknowledgment initial assertion state here */"; // true or false

    private static final String ALLOW_INVERSIONS_REPLACEMENT =
            "/* insert inversion permission flag here */"; // true or false

    private static final String HANDSHAKE_REACH =
            "// Checks whether the given request(s) and acknowledgement(s) form a handshake;\n" +
            "// optionally, one can allow inverting some of these signals and specify the initial phase of the handshake.\n" +
            "let\n" +
            "    // Non-empty set of request signal names.\n" +
            "    REQ_NAMES = {" + REQ_NAMES_REPLACEMENT + "},\n" +
            "    // Non-empty set of acknowledgement signal names.\n" +
            "    ACK_NAMES = {" + ACK_NAMES_REPLACEMENT + "},\n" +
            "    // The following two values specify the initial state of the handshake.\n" +
            "    REQ_INITIALLY_ASSERTED = " + REQ_INITIALLY_ASSERTED_REPLACEMENT + ",\n" +
            "    ACK_INITIALLY_ASSERTED = " + ACK_INITIALLY_ASSERTED_REPLACEMENT + ",\n" +
            "    // If true then arbitrary inversions of signals are allowed.\n" +
            "    ALLOW_INVERSIONS = " + ALLOW_INVERSIONS_REPLACEMENT + ",\n" +
            "\n" +
            "    // Auxiliary calculated set of requests.\n" +
            "    reqs = gather nm in REQ_NAMES { S nm },\n" +
            "    // Auxiliary calculated set of acknowledgements.\n" +
            "    acks = gather nm in ACK_NAMES { S nm },\n" +
            "\n" +
            "    // Handshake is active if the request is an output;\n" +
            "    // if there are several requests, they must all be of the same type.\n" +
            "    active = exists s in reqs { is_output s },\n" +
            "    // Some request is asserted (correcting for the polarity and initial state of handshake).\n" +
            "    req = exists s in reqs { $s ^ (is_init s ^ REQ_INITIALLY_ASSERTED) },\n" +
            "    // Some acknowledgement is asserted (correcting for the polarity and initial state of handshake).\n" +
            "    ack = exists s in acks { $s ^ (is_init s ^ ACK_INITIALLY_ASSERTED) },\n" +
            "\n" +
            "    // Request assert/withdraw/change is enabled (correcting for the polarity and initial state of handshake).\n" +
            "    en_req_assert = exists e in ev reqs s.t. (is_init sig e ^ REQ_INITIALLY_ASSERTED) ? is_minus e : is_plus e { @e },\n" +
            "    en_req_withdraw = exists e in ev reqs s.t. (is_init sig e ^ REQ_INITIALLY_ASSERTED)  ? is_plus e : is_minus e { @e },\n" +
            "    en_req = en_req_assert | en_req_withdraw,\n" +
            "    // Acknowledgement assert/withdraw/change is enabled (correcting for the polarity and initial state of handshake).\n" +
            "    en_ack_assert = exists e in ev acks s.t. (is_init sig e ^ ACK_INITIALLY_ASSERTED) ? is_minus e : is_plus e { @e },\n" +
            "    en_ack_withdraw = exists e in ev acks s.t. (is_init sig e ^ ACK_INITIALLY_ASSERTED) ? is_plus e : is_minus e { @e },\n" +
            "    en_ack = en_ack_assert | en_ack_withdraw\n" +
            "{\n" +
            "    // Check that all requests are of the same type.\n" +
            "    exists s in reqs { ~(active ? is_output s : is_input s) } ?\n" +
            "    fail \"The requests must be of the same type (either inputs or outputs)\" :\n" +
            "    // Check that all acknowledgement have the type opposite to that of requests.\n" +
            "    exists s in acks { ~(active ? is_input s : is_output s) } ?\n" +
            "    fail \"The acknowledgements must be of the opposite type (either inputs or outputs) to requests\" :\n" +
            "\n" +
            "    // Check that either inversions are allowed or the initial values of requests are equal to REQ_INITIALLY_ASSERTED.\n" +
            "    ~ALLOW_INVERSIONS & exists s in reqs { is_init s ^ REQ_INITIALLY_ASSERTED } ?\n" +
            "    fail \"The initial values of some request(s) are wrong and inversions are not allowed\" :\n" +
            "    // Check that either inversions are allowed or the initial values of acknowledgements are equal to ACK_INITIALLY_ASSERTED.\n" +
            "    ~ALLOW_INVERSIONS & exists s in acks { is_init s ^ ACK_INITIALLY_ASSERTED } ?\n" +
            "    fail \"The initial values of some acknowledgement(s) are wrong and inversions are not allowed\" :\n" +
            "\n" +
            "    // Check that the requests are 1-hot (correcting for the polarity and initial state of handshake).\n" +
            "    threshold s in reqs { $s ^ (is_init s ^ REQ_INITIALLY_ASSERTED) }\n" +
            "    |\n" +
            "    // Check that the acknowledgement are 1-hot (correcting for the polarity and initial state of handshake).\n" +
            "    threshold s in acks { $s ^ (is_init s ^ ACK_INITIALLY_ASSERTED) }\n" +
            "    |\n" +
            "    // Check that the handshake progresses as expected, i.e. signals are not enabled unexpectedly.\n" +
            "    ~req & ~ack & (en_req_withdraw | en_ack)\n" +
            "    |\n" +
            "     req & ~ack & (en_req | en_ack_withdraw)\n" +
            "    |\n" +
            "     req &  ack & (en_req_assert | en_ack)\n" +
            "    |\n" +
            "    ~req &  ack & (en_req | en_ack_assert)\n" +
            "    |\n" +
            "    // If the handshake is active, check that the appropriate acknowledgement edge is enabled immediately after request changes.\n" +
            "    active & (req & ~ack & ~en_ack_assert | ~req & ack & ~en_ack_withdraw)\n" +
            "    |\n" +
            "    // If the h/s is passive, check that the appropriate request edge is enabled immediately after acknowledgement changes.\n" +
            "    ~active & (req & ack & ~en_req_withdraw | ~req & ~ack & ~en_req_assert)" +
            "}\n";

    private static final String REQ_LABEL = "REQ";
    private static final String ACK_LABEL = "ACK";
    private static final String ASSERTED_LABEL = Character.toString((char) 0x2191);
    private static final String WITHDRAWN_LABEL = Character.toString((char) 0x2193);
    private static final String LABEL_PREFIX = "  ";
    private static final String TOOLTIP_PREFIX = "Before ";
    private static final String ASSERTED_SUFFIX = " asserted";
    private static final String WITHDRAWN_SUFFIX = " withdrawn";

    private JRadioButton passiveRadioButton;
    private SignalList inputList;
    private SignalList outputList;
    private JRadioButton stateReq1Ack0RadioButton;
    private JRadioButton stateReq1Ack1RadioButton;
    private JRadioButton stateReq0Ack1RadioButton;
    private JCheckBox inversionCheckbox;

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

    public HandshakeDialog(Window owner, Stg stg) {
        super(owner, "Handshake wizard", stg);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                updateOkEnableness();
                requestFocus();
            }
        });

    }

    @Override
    public JPanel createControlsPanel() {
        JPanel result = super.createControlsPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.setBorder(SizeHelper.getGapBorder());

        passiveRadioButton = new JRadioButton("passive");
        JRadioButton activeRadioButton = new JRadioButton("active");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(passiveRadioButton);
        buttonGroup.add(activeRadioButton);

        JPanel typePanel = new JPanel(GuiUtils.createFlowLayout());
        typePanel.add(new JLabel("Type:"));
        typePanel.add(passiveRadioButton);
        typePanel.add(activeRadioButton);

        Stg stg = getUserData();

        List<String> inputSignals = new ArrayList<>(stg.getSignalReferences(Signal.Type.INPUT));
        Collections.sort(inputSignals);
        inputList = new SignalList(inputSignals, SignalCommonSettings.getInputColor());
        inputList.addListSelectionListener(l -> updateOkEnableness());

        List<String> outputSignals = new ArrayList<>(stg.getSignalReferences(Signal.Type.OUTPUT));
        Collections.sort(outputSignals);
        outputList = new SignalList(outputSignals, SignalCommonSettings.getOutputColor());
        outputList.addListSelectionListener(l -> updateOkEnableness());

        JPanel signalPanel = new JPanel(GuiUtils.createGridLayout(1, 2));
        JScrollPane reqScroll = new JScrollPane();
        signalPanel.add(GuiUtils.createLabeledComponent(reqScroll, REQ_LABEL + ":", BorderLayout.NORTH));
        JScrollPane ackScroll = new JScrollPane();
        signalPanel.add(GuiUtils.createLabeledComponent(ackScroll, ACK_LABEL + ":", BorderLayout.NORTH));

        passiveRadioButton.addChangeListener(l -> {
            reqScroll.setViewportView(getReqList());
            ackScroll.setViewportView(getAckList());
        });
        passiveRadioButton.setSelected(true);

        JPanel statePanel = new JPanel(GuiUtils.createFlowLayout());
        ButtonGroup stateButtonGroup = new ButtonGroup();

        JRadioButton stateReq0Ack0RadioButton = new JRadioButton(LABEL_PREFIX + REQ_LABEL + ASSERTED_LABEL);
        stateReq0Ack0RadioButton.setToolTipText(TOOLTIP_PREFIX + REQ_LABEL + ASSERTED_SUFFIX);
        stateButtonGroup.add(stateReq0Ack0RadioButton);
        statePanel.add(stateReq0Ack0RadioButton);

        stateReq1Ack0RadioButton = new JRadioButton(LABEL_PREFIX + ACK_LABEL + ASSERTED_LABEL);
        stateReq1Ack0RadioButton.setToolTipText(TOOLTIP_PREFIX + ACK_LABEL + ASSERTED_SUFFIX);
        stateButtonGroup.add(stateReq1Ack0RadioButton);
        statePanel.add(stateReq1Ack0RadioButton);

        stateReq1Ack1RadioButton = new JRadioButton(LABEL_PREFIX + REQ_LABEL + WITHDRAWN_LABEL);
        stateReq1Ack1RadioButton.setToolTipText(TOOLTIP_PREFIX + REQ_LABEL + WITHDRAWN_SUFFIX);
        stateButtonGroup.add(stateReq1Ack1RadioButton);
        statePanel.add(stateReq1Ack1RadioButton);

        stateReq0Ack1RadioButton = new JRadioButton(LABEL_PREFIX + ACK_LABEL + WITHDRAWN_LABEL);
        stateReq0Ack1RadioButton.setToolTipText(TOOLTIP_PREFIX + ACK_LABEL + WITHDRAWN_SUFFIX);
        stateButtonGroup.add(stateReq0Ack1RadioButton);
        statePanel.add(stateReq0Ack1RadioButton);

        stateReq0Ack0RadioButton.setSelected(true);

        inversionCheckbox = new JCheckBox(" Allow arbitrary inversions of signals");

        JPanel optionsPanel = new JPanel(GuiUtils.createBorderLayout());
        optionsPanel.add(GuiUtils.createBorderedComponent(statePanel, "Initial state: "), BorderLayout.NORTH);
        optionsPanel.add(inversionCheckbox, BorderLayout.SOUTH);

        result.add(typePanel, BorderLayout.NORTH);
        result.add(signalPanel, BorderLayout.CENTER);
        result.add(optionsPanel, BorderLayout.SOUTH);

        return result;
    }

    public VerificationParameters getSettings() {

        String reach = HANDSHAKE_REACH
                .replace(REQ_NAMES_REPLACEMENT, getQuotedSelectedItems(getReqList()))
                .replace(ACK_NAMES_REPLACEMENT, getQuotedSelectedItems(getAckList()))
                .replace(REQ_INITIALLY_ASSERTED_REPLACEMENT, getReqAssertionState())
                .replace(ACK_INITIALLY_ASSERTED_REPLACEMENT, getAckAssertionState())
                .replace(ALLOW_INVERSIONS_REPLACEMENT, getInversionPermission());

        return new VerificationParameters("Handshake protocol",
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

    private SignalList getReqList() {
        return passiveRadioButton.isSelected() ? inputList : outputList;
    }

    private SignalList getAckList() {
        return passiveRadioButton.isSelected() ? outputList : inputList;
    }

    private String getReqAssertionState() {
        return stateReq1Ack0RadioButton.isSelected() || stateReq1Ack1RadioButton.isSelected() ? "true" : "false";
    }

    private String getAckAssertionState() {
        return stateReq0Ack1RadioButton.isSelected() || stateReq1Ack1RadioButton.isSelected() ? "true" : "false";
    }

    private String getInversionPermission() {
        return inversionCheckbox.isSelected() ? "true" : "false";
    }

    private void updateOkEnableness() {
        setOkEnableness((inputList != null) && !inputList.getSelectedValuesList().isEmpty()
                && (outputList != null) && !outputList.getSelectedValuesList().isEmpty());
    }

    private String getQuotedSelectedItems(JList<String> list) {
        return list.getSelectedValuesList().stream()
                .map(ref -> "\"" + ref + "\"")
                .collect(Collectors.joining(", "));
    }

}
