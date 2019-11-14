package org.workcraft.plugins.mpsat.gui;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.dialogs.ModalDialog;
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

public class AssertionDialog extends ModalDialog<MpsatPresetManager> {

    private JTextArea propertyText;

    public AssertionDialog(Window owner, MpsatPresetManager presetManager) {
        super(owner, "Custom assertion", presetManager);
    }

    @Override
    public JPanel createControlsPanel() {
        JPanel result = super.createControlsPanel();
        result.setLayout(GuiUtils.createBorderLayout());

        PresetManagerPanel<VerificationParameters> presetPanel = createPresetPanel();

        result.add(presetPanel, BorderLayout.NORTH);
        result.add(createAssertionPanel(), BorderLayout.CENTER);

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

        SettingsToControlsMapper<VerificationParameters> guiMapper = new SettingsToControlsMapper<VerificationParameters>() {
            @Override
            public void applySettingsToControls(VerificationParameters settings) {
                AssertionDialog.this.applySettingsToControls(settings);
            }

            @Override
            public VerificationParameters getSettingsFromControls() {
                return AssertionDialog.this.getSettingsFromControls();
            }
        };

        return new PresetManagerPanel<>(getUserData(), builtInPresets, guiMapper);
    }

    private JPanel createAssertionPanel() {
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
    }

    private VerificationParameters getSettingsFromControls() {
        return new VerificationParameters(null, VerificationMode.ASSERTION,
                0, SolutionMode.MINIMUM_COST, 0, propertyText.getText(), true);
    }

}
