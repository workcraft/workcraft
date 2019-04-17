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
public class AssertionDialog extends JDialog {

    private final MpsatPresetManager presetManager;
    private JPanel predicatePanel, buttonsPanel;
    private PresetManagerPanel<VerificationParameters> presetPanel;
    private JTextArea assertionText;
    private boolean modalResult;

    public AssertionDialog(Window owner, MpsatPresetManager presetManager) {
        super(owner, "Custom assertion", ModalityType.APPLICATION_MODAL);
        this.presetManager = presetManager;

        createPresetPanel();
        createAssertionPanel();
        createButtonsPanel();

        int buttonPanelHeight = buttonsPanel.getPreferredSize().height;
        double[][] size = new double[][] {
            {TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.FILL, buttonPanelHeight},
        };

        final TableLayout layout = new TableLayout(size);
        layout.setHGap(SizeHelper.getLayoutHGap());
        layout.setVGap(SizeHelper.getLayoutVGap());

        JPanel contentPanel = new JPanel(layout);
        contentPanel.setBorder(SizeHelper.getEmptyBorder());

        contentPanel.add(presetPanel, "0 0");
        contentPanel.add(predicatePanel, "0 1");
        contentPanel.add(buttonsPanel, "0 2");

        setContentPane(contentPanel);

        presetPanel.selectFirst();

        getRootPane().registerKeyboardAction(event -> {
            modalResult = false;
            setVisible(false);
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                assertionText.requestFocus();
            }
        });
        setMinimumSize(new Dimension(450, 350));
        pack();
        setLocationRelativeTo(owner);
    }

    private void createPresetPanel() {
        ArrayList<Preset<VerificationParameters>> builtInPresets = new ArrayList<>();

        builtInPresets.add(new Preset<>("", VerificationParameters.getEmptyAssertionSettings(), false));

        SettingsToControlsMapper<VerificationParameters> guiMapper = new SettingsToControlsMapper<VerificationParameters>() {
            @Override
            public void applySettingsToControls(VerificationParameters settings) {
                AssertionDialog.this.applySettingsToControls(settings);
            }

            @Override
            public VerificationParameters getSettingsFromControls() {
                VerificationParameters settings = AssertionDialog.this.getSettingsFromControls();
                return settings;
            }
        };

        presetPanel = new PresetManagerPanel<VerificationParameters>(presetManager, builtInPresets, guiMapper, this);
    }

    private void createAssertionPanel() {
        predicatePanel = new JPanel(new BorderLayout());
        String title = "Assertion (use '" + NamespaceHelper.getHierarchySeparator() + "' as hierarchy separator)";
        predicatePanel.setBorder(SizeHelper.getTitledBorder(title));

        assertionText = new JTextArea();
        assertionText.setMargin(SizeHelper.getTextMargin());
        assertionText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        assertionText.setText("");
        assertionText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() > 127) {
                    e.consume();  // ignore non-ASCII characters
                }
            }
        });
        JScrollPane assertionScrollPane = new JScrollPane(assertionText);

        JPanel propertyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,
                SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));

        predicatePanel.add(assertionScrollPane, BorderLayout.CENTER);
        predicatePanel.add(propertyPanel, BorderLayout.SOUTH);
    }

    public VerificationParameters getSettings() {
        return getSettingsFromControls();
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
        helpButton.addActionListener(event -> DesktopApi.open(new File("help/assertion.html")));

        buttonsPanel.add(runButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(helpButton);
    }

    private void applySettingsToControls(VerificationParameters settings) {
        assertionText.setText(settings.getExpression());
    }

    private VerificationParameters getSettingsFromControls() {
        return new VerificationParameters(null, VerificationMode.ASSERTION,
                0, SolutionMode.MINIMUM_COST, 0, assertionText.getText(), true);
    }

    public boolean reveal() {
        setVisible(true);
        return modalResult;
    }

}
