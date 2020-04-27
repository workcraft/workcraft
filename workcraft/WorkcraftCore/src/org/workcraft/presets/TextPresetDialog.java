package org.workcraft.presets;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.controls.FlatTextArea;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collections;

public class TextPresetDialog extends PresetDialog<String> {

    private PresetManagerPanel<String> presetPanel;
    private FlatTextArea textArea;
    private JPanel buttonsPanel;

    public TextPresetDialog(Window owner, String title, PresetManager<String> presetManager) {
        this(owner, title, presetManager, null);
    }

    public TextPresetDialog(Window owner, String title, PresetManager<String> presetManager, File helpFile) {
        super(owner, title, presetManager);
        presetPanel.selectFirst();
        textArea.setCaretPosition(0);
        textArea.requestFocus();
        if (helpFile != null) {
            JButton helpButton = GuiUtils.createDialogButton("Help");
            helpButton.addActionListener(event -> DesktopApi.open(helpFile));
            buttonsPanel.add(helpButton);
        }
    }

    @Override
    public JPanel createContentPanel() {
        JPanel result = super.createContentPanel();
        result.setLayout(GuiUtils.createBorderLayout());
        result.add(createTextPanel(), BorderLayout.CENTER);
        // Preset panel has to be created the last as its guiMapper refers to other controls
        presetPanel = createPresetPanel();
        result.add(presetPanel, BorderLayout.NORTH);
        return result;
    }

    private PresetManagerPanel<String> createPresetPanel() {
        DataMapper<String> guiMapper = new DataMapper<String>() {
            @Override
            public void applyDataToControls(String data) {
                textArea.setText(data);
                textArea.setCaretPosition(0);
                textArea.requestFocus();
                textArea.discardEditHistory();
            }

            @Override
            public String getDataFromControls() {
                return textArea.getText();
            }
        };

        return new PresetManagerPanel<>(getUserData(), guiMapper);
    }

    private JPanel createTextPanel() {
        textArea = new FlatTextArea();
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
    public String getPresetData() {
        return textArea.getText();
    }

    @Override
    public JPanel createButtonsPanel() {
        buttonsPanel = super.createButtonsPanel();
        return buttonsPanel;
    }

}
