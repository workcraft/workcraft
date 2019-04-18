package org.workcraft.plugins.mpsat.gui;

import info.clearthought.layout.TableLayout;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.VerificationParameters.SolutionMode;
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

@SuppressWarnings("serial")
public class PropertyDialog extends JDialog {
    private static final int DEFAULT_ALL_SOLUTION_LIMIT = 10;

    private final MpsatPresetManager presetManager;
    private JPanel optionsPanel, predicatePanel, buttonsPanel;
    private PresetManagerPanel<VerificationParameters> presetPanel;
    private JComboBox<VerificationMode> modeCombo;
    private JTextField solutionLimitText;
    private JTextArea propertyText;
    private JRadioButton allSolutionsRadioButton, firstSolutionRadioButton, cheapestSolutionRadioButton;
    private JRadioButton satisfiebleRadioButton, unsatisfiebleRadioButton;
    private boolean modalResult;

    public PropertyDialog(Window owner, MpsatPresetManager presetManager) {
        super(owner, "Custom property", ModalityType.APPLICATION_MODAL);
        this.presetManager = presetManager;

        createPresetPanel();
        createOptionsPanel();
        createPropertyPanel();
        createButtonsPanel();

        int buttonPanelHeight = buttonsPanel.getPreferredSize().height;
        double[][] size = new double[][] {
            {TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, buttonPanelHeight},
        };
        final TableLayout layout = new TableLayout(size);
        layout.setHGap(SizeHelper.getLayoutHGap());
        layout.setVGap(SizeHelper.getLayoutVGap());

        JPanel contentPanel = new JPanel(layout);
        contentPanel.setBorder(SizeHelper.getEmptyBorder());

        contentPanel.add(presetPanel, "0 0");
        contentPanel.add(optionsPanel, "0 1");
        contentPanel.add(predicatePanel, "0 2");
        contentPanel.add(buttonsPanel, "0 3");

        setContentPane(contentPanel);

        presetPanel.selectFirst();

        getRootPane().registerKeyboardAction(event -> {
            modalResult = false;
            setVisible(false);
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                propertyText.requestFocus();
            }
        });

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    private void createPresetPanel() {
        ArrayList<Preset<VerificationParameters>> builtInPresets = new ArrayList<>();

        if (presetManager.isAllowStgPresets()) {
            builtInPresets.add(new Preset<>("Consistency",
                    VerificationParameters.getConsistencySettings(), true));

            builtInPresets.add(new Preset<>("Delay insensitive interface",
                    VerificationParameters.getDiInterfaceSettings(), true));

            builtInPresets.add(new Preset<>("Input properness",
                    VerificationParameters.getInputPropernessSettings(), true));

            builtInPresets.add(new Preset<>("Output persistency (without dummies)",
                    VerificationParameters.getOutputPersistencySettings(), true));
        }

        builtInPresets.add(new Preset<>("Deadlock freeness",
                VerificationParameters.getDeadlockReachSettings(), true));

        builtInPresets.add(new Preset<>("Deadlock freeness without maximal dummies",
                VerificationParameters.getDeadlockWithoutMaximalDummyReachSettings(), true));

        SettingsToControlsMapper<VerificationParameters> guiMapper = new SettingsToControlsMapper<VerificationParameters>() {
            @Override
            public void applySettingsToControls(VerificationParameters settings) {
                PropertyDialog.this.applySettingsToControls(settings);
            }

            @Override
            public VerificationParameters getSettingsFromControls() {
                VerificationParameters settings = PropertyDialog.this.getSettingsFromControls();
                return settings;
            }
        };

        presetPanel = new PresetManagerPanel<>(presetManager, builtInPresets, guiMapper, this);
    }

    private void createOptionsPanel() {
        optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setBorder(SizeHelper.getTitledBorder("MPSat settings"));

        modeCombo = new JComboBox<>();
        modeCombo.setEditable(false);
        if (presetManager.isAllowStgPresets()) {
            modeCombo.addItem(VerificationMode.STG_REACHABILITY);
        }
        modeCombo.addItem(VerificationMode.REACHABILITY);

        optionsPanel.add(GuiUtils.createWideLabeledComponent(modeCombo, "  Mode:      "), BorderLayout.NORTH);

        JPanel solutionModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
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
        optionsPanel.add(solutionModePanel, BorderLayout.SOUTH);
    }

    private void createPropertyPanel() {
        predicatePanel = new JPanel(new BorderLayout());
        String title = "Reach predicate (use '" + NamespaceHelper.getHierarchySeparator() + "' as hierarchy separator)";
        predicatePanel.setBorder(SizeHelper.getTitledBorder(title));

        propertyText = new JTextArea();
        propertyText.setMargin(SizeHelper.getTextMargin());
        propertyText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        propertyText.setText("");
        propertyText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() > 127) {
                    e.consume();  // ignore non-ASCII characters
                }
            }
        });
        JScrollPane propertyScrollPane = new JScrollPane(propertyText);

        satisfiebleRadioButton = new JRadioButton("satisfiable");
        unsatisfiebleRadioButton = new JRadioButton("unsatisfiable");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(satisfiebleRadioButton);
        buttonGroup.add(unsatisfiebleRadioButton);
        unsatisfiebleRadioButton.setSelected(true);

        JPanel propertyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
                SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));

        propertyPanel.add(new JLabel("Property holds if predicate is:"));
        propertyPanel.add(satisfiebleRadioButton);
        propertyPanel.add(unsatisfiebleRadioButton);

        predicatePanel.add(propertyScrollPane, BorderLayout.CENTER);
        predicatePanel.add(propertyPanel, BorderLayout.SOUTH);
    }

    private void createButtonsPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton runButton = GuiUtils.createDialogButton("Run");
        runButton.addActionListener(event -> {
            modalResult = true;
            setVisible(false);
        });

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> {
            modalResult = false;
            setVisible(false);
        });

        JButton helpButton = GuiUtils.createDialogButton("Help");
        helpButton.addActionListener(event -> DesktopApi.open(new File("help/reach.html")));

        buttonsPanel.add(runButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(helpButton);
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
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
            unsatisfiebleRadioButton.setSelected(true);
        } else {
            satisfiebleRadioButton.setSelected(true);
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

        VerificationParameters settings = new VerificationParameters(null, (VerificationMode) modeCombo.getSelectedItem(),
                0, solutionMode, solutionLimin, propertyText.getText(), unsatisfiebleRadioButton.isSelected());

        return settings;
    }

}
