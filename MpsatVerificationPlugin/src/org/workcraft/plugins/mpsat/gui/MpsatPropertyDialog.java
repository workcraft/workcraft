package org.workcraft.plugins.mpsat.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatParameters.SolutionMode;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.shared.gui.PresetManagerPanel;
import org.workcraft.plugins.shared.presets.Preset;
import org.workcraft.plugins.shared.presets.SettingsToControlsMapper;
import org.workcraft.util.GUI;
import org.workcraft.util.IntDocument;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class MpsatPropertyDialog extends JDialog {
    private static final int DEFAULT_ALL_SOLUTION_LIMIT = 10;

    private JPanel optionsPanel, predicatePanel, buttonsPanel;
    private PresetManagerPanel<MpsatParameters> presetPanel;
    private JComboBox<MpsatMode> modeCombo;
    private JTextField solutionLimitText;
    private JTextArea propertyText;
    private JRadioButton allSolutionsRadioButton, firstSolutionRadioButton, cheapestSolutionRadioButton;
    private JRadioButton satisfiebleRadioButton, unsatisfiebleRadioButton;
    private final MpsatPresetManager presetManager;

    private int modalResult = 0;

    public MpsatPropertyDialog(Window owner, MpsatPresetManager presetManager) {
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

        getRootPane().registerKeyboardAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        modalResult = 0;
                        setVisible(false);
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                propertyText.requestFocus();
            }
        });
        setMinimumSize(new Dimension(450, 450));
    }

    private void createPresetPanel() {
        ArrayList<Preset<MpsatParameters>> builtInPresets = new ArrayList<>();

        if (presetManager.isAllowStgPresets()) {
            builtInPresets.add(new Preset<>("Consistency",
                    MpsatParameters.getConsistencySettings(), true));

            builtInPresets.add(new Preset<>("Delay insensitive interface",
                    MpsatParameters.getDiInterfaceSettings(), true));

            builtInPresets.add(new Preset<>("Input properness",
                    MpsatParameters.getInputPropernessSettings(), true));

            builtInPresets.add(new Preset<>("Output persistency (without dummies)",
                    MpsatParameters.getOutputPersistencySettings(), true));
        }

        builtInPresets.add(new Preset<>("Deadlock freeness",
                MpsatParameters.getDeadlockReachSettings(), true));

        builtInPresets.add(new Preset<>("Deadlock freeness without maximal dummies",
                MpsatParameters.getDeadlockWithoutMaximalDummyReachSettings(), true));

        SettingsToControlsMapper<MpsatParameters> guiMapper = new SettingsToControlsMapper<MpsatParameters>() {
            @Override
            public void applySettingsToControls(MpsatParameters settings) {
                MpsatPropertyDialog.this.applySettingsToControls(settings);
            }

            @Override
            public MpsatParameters getSettingsFromControls() {
                MpsatParameters settings = MpsatPropertyDialog.this.getSettingsFromControls();
                return settings;
            }
        };

        presetPanel = new PresetManagerPanel<MpsatParameters>(presetManager, builtInPresets, guiMapper, this);
    }

    private void createOptionsPanel() {
        optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setBorder(SizeHelper.getTitledBorder("MPSat settings"));

        modeCombo = new JComboBox<MpsatMode>();
        modeCombo.setEditable(false);
        if (presetManager.isAllowStgPresets()) {
            modeCombo.addItem(MpsatMode.STG_REACHABILITY);
        }
        modeCombo.addItem(MpsatMode.REACHABILITY);

        optionsPanel.add(GUI.createWideLabeledComponent(modeCombo, "  Mode:      "), BorderLayout.NORTH);

        JPanel solutionModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        solutionModePanel.add(new JLabel("Solution:"));
        cheapestSolutionRadioButton = new JRadioButton("minimise cost function");
        cheapestSolutionRadioButton.setSelected(true);
        cheapestSolutionRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                solutionLimitText.setEnabled(false);
            }
        });
        firstSolutionRadioButton = new JRadioButton("any");
        firstSolutionRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                solutionLimitText.setEnabled(false);
            }
        });
        allSolutionsRadioButton = new JRadioButton("all");
        allSolutionsRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                solutionLimitText.setEnabled(true);
            }
        });
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

        JButton runButton = GUI.createDialogButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modalResult = 1;
                setVisible(false);
            }
        });

        JButton cancelButton = GUI.createDialogButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modalResult = 0;
                setVisible(false);
            }
        });

        JButton helpButton = GUI.createDialogButton("Help");
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DesktopApi.open(new File("help/reach.html"));
            }
        });

        buttonsPanel.add(runButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(helpButton);
    }

    public int getModalResult() {
        return modalResult;
    }

    public MpsatParameters getSettings() {
        return getSettingsFromControls();
    }

    private void applySettingsToControls(MpsatParameters settings) {
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

    private MpsatParameters getSettingsFromControls() {
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

        MpsatParameters settings = new MpsatParameters(null, (MpsatMode) modeCombo.getSelectedItem(),
                0, solutionMode, solutionLimin, propertyText.getText(), unsatisfiebleRadioButton.isSelected());

        return settings;
    }

}
