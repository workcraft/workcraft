package org.workcraft.plugins.mpsat.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.VerificationParameters.SolutionMode;
import org.workcraft.plugins.mpsat.utils.ReachUtils;
import org.workcraft.presets.Preset;
import org.workcraft.presets.PresetManagerPanel;
import org.workcraft.presets.SettingsToControlsMapper;
import org.workcraft.shared.IntDocument;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

public class PropertyDialog extends ModalDialog<MpsatPresetManager> {

    private static final int DEFAULT_ALL_SOLUTION_LIMIT = 10;

    private static VerificationParameters autoSavedProperty = null;

    private JComboBox<VerificationMode> modeCombo;
    private JTextField solutionLimitText;
    private JTextArea propertyText;
    private JRadioButton allSolutionsRadioButton;
    private JRadioButton firstSolutionRadioButton;
    private JRadioButton cheapestSolutionRadioButton;
    private JRadioButton satisfiableRadioButton;
    private JRadioButton unsatisfiableRadioButton;

    public PropertyDialog(Window owner, MpsatPresetManager presetManager) {
        super(owner, "Custom property", presetManager);
    }

    @Override
    public JPanel createControlsPanel() {
        JPanel result = super.createControlsPanel();
        result.setLayout(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL},
                new double[]{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}));

        PresetManagerPanel<VerificationParameters> presetPanel = createPresetPanel();

        result.add(presetPanel, new TableLayoutConstraints(0, 0));
        result.add(createOptionsPanel(), new TableLayoutConstraints(0, 1));
        result.add(createPropertyPanel(), new TableLayoutConstraints(0, 2));

        presetPanel.selectFirst();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                if (propertyText.getText().isEmpty()) {
                    propertyText.setText("\n\n");
                }
                propertyText.setCaretPosition(0);
                propertyText.requestFocus();
            }
        });

        return result;
    }

    private PresetManagerPanel<VerificationParameters> createPresetPanel() {
        ArrayList<Preset<VerificationParameters>> builtInPresets = new ArrayList<>();

        if (autoSavedProperty != null) {
            builtInPresets.add(new Preset<>("Auto-saved property",
                    autoSavedProperty, true));
        }

        MpsatPresetManager presetManager = getUserData();
        if (presetManager.isAllowStgPresets()) {
            builtInPresets.add(new Preset<>("Consistency",
                    ReachUtils.getConsistencySettings(), true));

            builtInPresets.add(new Preset<>("Delay insensitive interface",
                    ReachUtils.getDiInterfaceSettings(), true));

            builtInPresets.add(new Preset<>("Input properness",
                    ReachUtils.getInputPropernessSettings(), true));

            builtInPresets.add(new Preset<>("Output persistency (without dummies)",
                    ReachUtils.getOutputPersistencySettings(), true));
        }

        builtInPresets.add(new Preset<>("Deadlock freeness",
                ReachUtils.getDeadlockReachSettings(), true));

        builtInPresets.add(new Preset<>("Deadlock freeness without maximal dummies",
                ReachUtils.getDeadlockWithoutMaximalDummyReachSettings(), true));

        SettingsToControlsMapper<VerificationParameters> guiMapper = new SettingsToControlsMapper<VerificationParameters>() {
            @Override
            public void applySettingsToControls(VerificationParameters settings) {
                PropertyDialog.this.applySettingsToControls(settings);
            }

            @Override
            public VerificationParameters getSettingsFromControls() {
                return PropertyDialog.this.getSettingsFromControls();
            }
        };

        return new PresetManagerPanel<>(presetManager, builtInPresets, guiMapper);
    }

    private JPanel createOptionsPanel() {
        JPanel result = new JPanel(new BorderLayout());
        result.setBorder(SizeHelper.getTitledBorder("MPSat settings"));

        modeCombo = new JComboBox<>();
        modeCombo.setEditable(false);
        if (getUserData().isAllowStgPresets()) {
            modeCombo.addItem(VerificationMode.STG_REACHABILITY);
        }
        modeCombo.addItem(VerificationMode.REACHABILITY);

        result.add(GuiUtils.createLabeledComponent(modeCombo, "  Mode:      "), BorderLayout.NORTH);

        JPanel solutionModePanel = new JPanel(GuiUtils.createFlowLayout());
        solutionModePanel.add(new JLabel("Solution:"));
        cheapestSolutionRadioButton = new JRadioButton("minimise cost function");
        cheapestSolutionRadioButton.setSelected(true);
        cheapestSolutionRadioButton.addActionListener(event -> solutionLimitText.setEnabled(false));
        firstSolutionRadioButton = new JRadioButton("any");
        firstSolutionRadioButton.addActionListener(event -> solutionLimitText.setEnabled(false));
        allSolutionsRadioButton = new JRadioButton("all");
        allSolutionsRadioButton.addActionListener(event -> solutionLimitText.setEnabled(true));
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(cheapestSolutionRadioButton);
        buttonGroup.add(firstSolutionRadioButton);
        buttonGroup.add(allSolutionsRadioButton);
        solutionModePanel.add(cheapestSolutionRadioButton);
        solutionModePanel.add(firstSolutionRadioButton);
        solutionModePanel.add(allSolutionsRadioButton);

        solutionLimitText = new JTextField();
        Dimension dimension = solutionLimitText.getPreferredSize();
        dimension.width = 3 * dimension.height;
        solutionLimitText.setPreferredSize(dimension);
        solutionLimitText.setToolTipText("Maximum number of solutions. Leave blank for no limit.");
        solutionLimitText.setDocument(new IntDocument(3));
        solutionLimitText.setEnabled(false);
        solutionModePanel.add(solutionLimitText);
        result.add(solutionModePanel, BorderLayout.SOUTH);
        return result;
    }

    public JPanel createPropertyPanel() {
        JPanel resutl = new JPanel(new BorderLayout());
        String title = "Reach predicate (use '" + NamespaceHelper.getHierarchySeparator() + "' as hierarchy separator)";
        resutl.setBorder(SizeHelper.getTitledBorder(title));

        propertyText = new JTextArea();
        propertyText.setMargin(SizeHelper.getTextMargin());
        propertyText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        propertyText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() > 127) {
                    e.consume();  // ignore non-ASCII characters
                }
            }
        });
        JScrollPane propertyScrollPane = new JScrollPane(propertyText);

        satisfiableRadioButton = new JRadioButton("satisfiable");
        unsatisfiableRadioButton = new JRadioButton("unsatisfiable");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(satisfiableRadioButton);
        buttonGroup.add(unsatisfiableRadioButton);
        unsatisfiableRadioButton.setSelected(true);

        JPanel propertyPanel = new JPanel(GuiUtils.createFlowLayout());

        propertyPanel.add(new JLabel("Property holds if predicate is:"));
        propertyPanel.add(satisfiableRadioButton);
        propertyPanel.add(unsatisfiableRadioButton);

        resutl.add(propertyScrollPane, BorderLayout.CENTER);
        resutl.add(propertyPanel, BorderLayout.SOUTH);
        return resutl;
    }

    @Override
    public JPanel createButtonsPanel() {
        JPanel result = super.createButtonsPanel();
        JButton helpButton = GuiUtils.createDialogButton("Help");
        helpButton.addActionListener(event -> DesktopApi.open(new File("help/reach.html")));
        result.add(helpButton);
        return result;
    }

    public VerificationParameters getSettings() {
        return getSettingsFromControls();
    }

    private void applySettingsToControls(VerificationParameters settings) {
        modeCombo.setSelectedItem(settings.getMode());

        switch (settings.getSolutionMode()) {
        case MINIMUM_COST:
            cheapestSolutionRadioButton.setSelected(true);
            solutionLimitText.setText(Integer.toString(DEFAULT_ALL_SOLUTION_LIMIT));
            solutionLimitText.setEnabled(false);
            break;
        case FIRST:
            firstSolutionRadioButton.setSelected(true);
            solutionLimitText.setText(Integer.toString(DEFAULT_ALL_SOLUTION_LIMIT));
            solutionLimitText.setEnabled(false);
            break;
        case ALL:
            allSolutionsRadioButton.setSelected(true);
            int n = settings.getSolutionNumberLimit();
            solutionLimitText.setText((n > 0) ? Integer.toString(n) : "");
            solutionLimitText.setEnabled(true);
            break;
        }

        propertyText.setText(settings.getExpression());
        if (settings.getInversePredicate()) {
            unsatisfiableRadioButton.setSelected(true);
        } else {
            satisfiableRadioButton.setSelected(true);
        }
    }

    private VerificationParameters getSettingsFromControls() {
        SolutionMode solutionMode;
        if (firstSolutionRadioButton.isSelected()) {
            solutionMode = SolutionMode.FIRST;
        } else if (cheapestSolutionRadioButton.isSelected()) {
            solutionMode = SolutionMode.MINIMUM_COST;
        } else {
            solutionMode = SolutionMode.ALL;
        }

        int solutionLimin;
        try {
            solutionLimin = Integer.parseInt(solutionLimitText.getText());
        } catch (NumberFormatException e) {
            solutionLimin = 0;
        }
        if (solutionLimin < 0) {
            solutionLimin = 0;
        }

        return new VerificationParameters(null, (VerificationMode) modeCombo.getSelectedItem(),
                0, solutionMode, solutionLimin, propertyText.getText(), unsatisfiableRadioButton.isSelected());
    }

    @Override
    public boolean okAction() {
        if (super.okAction()) {
            autoSavedProperty = getSettings();
            return true;
        }
        return false;
    }

}
