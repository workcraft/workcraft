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
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class AssertionDialog extends ModalDialog<MpsatPresetManager> {

    private static VerificationParameters autoPreservedParameters = null;

    private PresetManagerPanel<VerificationParameters> presetPanel;
    private JTextArea propertyText;

    public AssertionDialog(Window owner, MpsatPresetManager presetManager) {
        super(owner, "Custom assertion", presetManager);
        initialise();
    }

    private void initialise() {
        presetPanel.selectFirst();
        propertyText.setCaretPosition(0);
        propertyText.requestFocus();
    }

    @Override
    public JPanel createControlsPanel() {
        JPanel result = super.createControlsPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        presetPanel = createPresetPanel();
        result.add(presetPanel, BorderLayout.NORTH);
        result.add(createAssertionPanel(), BorderLayout.CENTER);
        return result;
    }

    private PresetManagerPanel<VerificationParameters> createPresetPanel() {
        ArrayList<Preset<VerificationParameters>> builtInPresets = new ArrayList<>();
        if (autoPreservedParameters != null) {
            builtInPresets.add(new Preset<>("Auto-preserved configuration",
                    autoPreservedParameters, true));
        }

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

    @Override
    public boolean okAction() {
        if (super.okAction()) {
            autoPreservedParameters = getSettings();
            return true;
        }
        return false;
    }

}
