package org.workcraft.presets;

import javax.swing.*;

public class TextDataMapper implements DataMapper<String> {

    private final JTextArea textArea;

    public TextDataMapper(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void applyDataToControls(String data) {
        textArea.setText(data);
        textArea.setCaretPosition(0);
        textArea.requestFocus();
    }

    @Override
    public String getDataFromControls() {
        return textArea.getText();
    }

}
