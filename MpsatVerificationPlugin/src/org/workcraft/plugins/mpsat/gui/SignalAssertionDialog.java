package org.workcraft.plugins.mpsat.gui;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.VerificationParameters.SolutionMode;
import org.workcraft.presets.DataMapper;
import org.workcraft.presets.PresetManagerPanel;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collections;

public class SignalAssertionDialog extends ModalDialog<MpsatPresetManager> {

    private PresetManagerPanel<VerificationParameters> presetPanel;
    private JTextArea propertyText;

    public SignalAssertionDialog(Window owner, MpsatPresetManager presetManager) {
        super(owner, "Signal assertion", presetManager);
        presetPanel.selectFirst();
        propertyText.setCaretPosition(0);
        propertyText.requestFocus();
    }

    @Override
    public JPanel createControlsPanel() {
        JPanel result = super.createControlsPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.add(createAssertionPanel(), BorderLayout.CENTER);
        // Preset panel has to be created the last as its guiMapper refers to other controls
        presetPanel = createPresetPanel();
        result.add(presetPanel, BorderLayout.NORTH);
        return result;
    }

    private PresetManagerPanel<VerificationParameters> createPresetPanel() {
        MpsatPresetManager presetManager = getUserData();
        addExample(presetManager, "Mutual exclusion of signals",
                "// Signals u and v are mutually exclusive\n"
                + "!u || !v");

        DataMapper<VerificationParameters> guiMapper = new DataMapper<VerificationParameters>() {
            @Override
            public void applyDataToControls(VerificationParameters settings) {
                SignalAssertionDialog.this.applySettingsToControls(settings);
            }

            @Override
            public VerificationParameters getDataFromControls() {
                return SignalAssertionDialog.this.getSettingsFromControls();
            }
        };

        return new PresetManagerPanel<>(presetManager, guiMapper);
    }

    private void addExample(MpsatPresetManager presetManager, String title, String expression) {
        VerificationParameters settings = new VerificationParameters(title,
                VerificationMode.ASSERTION, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                expression, true);

        presetManager.addExample(title, settings);
    }

    private JPanel createAssertionPanel() {
        propertyText = new JTextArea();
        propertyText.setMargin(SizeHelper.getTextMargin());
        propertyText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        propertyText.setText(String.join("", Collections.nCopies(4, "\n")));
        propertyText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() > 127) {
                    e.consume();  // ignore non-ASCII characters
                }
            }
        });
        JScrollPane assertionScrollPane = new JScrollPane(propertyText);
        String title = "Assertion (use '" + NamespaceHelper.getHierarchySeparator() + "' as hierarchy separator)";
        return GuiUtils.createBorderedComponent(assertionScrollPane, title);
    }

    public VerificationParameters getSettings() {
        return getSettingsFromControls();
    }

    @Override
    public JPanel createButtonsPanel() {
        JPanel result = super.createButtonsPanel();

        JButton helpButton = GuiUtils.createDialogButton("Help");
        helpButton.addActionListener(event -> DesktopApi.open(new File("help/assertion.html")));
        result.add(helpButton);

        return result;
    }

    private void applySettingsToControls(VerificationParameters settings) {
        propertyText.setText(settings.getExpression());
        propertyText.setCaretPosition(0);
        propertyText.requestFocus();
    }

    private VerificationParameters getSettingsFromControls() {
        return new VerificationParameters(null, VerificationMode.ASSERTION,
                0, SolutionMode.MINIMUM_COST, 0, propertyText.getText(), true);
    }

}
