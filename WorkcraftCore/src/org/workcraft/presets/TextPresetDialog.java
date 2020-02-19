package org.workcraft.presets;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.dialogs.ModalDialog;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class TextPresetDialog extends ModalDialog<PresetManager<String>> implements PresetDialog<String> {

    private final File helpFile;

    private PresetManagerPanel<String> presetPanel;
    private JTextArea textArea;

    public TextPresetDialog(Window owner, String title, PresetManager<String> presetManager) {
        this(owner, title, presetManager, null);
    }

    public TextPresetDialog(Window owner, String title, PresetManager<String> presetManager, File helpFile) {
        super(owner, title, presetManager);
        this.helpFile = helpFile;
        initialise();
    }

    private void initialise() {
        presetPanel.selectFirst();
        textArea.setCaretPosition(0);
        textArea.requestFocus();
    }

    @Override
    public JPanel createControlsPanel() {
        JPanel result = super.createControlsPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.add(createTextPanel(), BorderLayout.CENTER);
        presetPanel = createPresetPanel();
        result.add(presetPanel, BorderLayout.NORTH);
        return result;
    }

    private PresetManagerPanel<String> createPresetPanel() {
        ArrayList<Preset<String>> builtInPresets = new ArrayList<>();
        DataMapper<String> guiMapper = new TextDataMapper(textArea);
        return new PresetManagerPanel<>(getUserData(), builtInPresets, guiMapper);
    }

    private JPanel createTextPanel() {
        textArea = new JTextArea();
        textArea.setMargin(SizeHelper.getTextMargin());
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        textArea.setText(String.join("", Collections.nCopies(4, "\n")));
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() > 127) {
                    e.consume();  // ignore non-ASCII characters
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(textArea);
        JPanel panel = new JPanel(GuiUtils.createBorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public String getData() {
        return textArea.getText();
    }

    @Override
    public JPanel createButtonsPanel() {
        JPanel result = super.createButtonsPanel();
        if (helpFile != null) {
            JButton helpButton = GuiUtils.createDialogButton("Help");
            helpButton.addActionListener(event -> DesktopApi.open(helpFile));
            result.add(helpButton);
        }
        return result;
    }

}
