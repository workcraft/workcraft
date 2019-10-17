package org.workcraft.gui.properties;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ColorComboBoxEditor implements ComboBoxEditor, ActionListener {

    static class Approximator implements ActionListener {
        public ActionListener target = null;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (target != null) {
                target.actionPerformed(e);
            }
        }
    }

    static class PreviewPanel extends JPanel {
        private final JLabel fgLabel;
        private final JLabel bgLabel;

        PreviewPanel(String fgText, Color fgColor, String bgText, Color bgColor) {
            super(new GridLayout(1, 0, SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, SizeHelper.getPreviewFontSize());

            fgLabel = new JLabel(fgText, JLabel.CENTER);
            fgLabel.setOpaque(true);
            fgLabel.setFont(font);
            fgLabel.setBackground(fgColor);
            add(fgLabel);

            bgLabel = new JLabel(bgText, JLabel.CENTER);
            bgLabel.setOpaque(true);
            bgLabel.setFont(font);
            bgLabel.setForeground(bgColor);
            add(bgLabel);
        }

        @Override
        public void setForeground(Color color) {
            super.setForeground(color);
            if (fgLabel != null) {
                fgLabel.setForeground(color);
                bgLabel.setBackground(color);
            }
        }
    }

    private static final String TAG_EDIT = "edit";
    private static final Approximator approx = new Approximator();
    private static final JColorChooser chooser = new JColorChooser();
    private static final JDialog dialog;

    static {
        Color fgColor = EditorCommonSettings.getBackgroundColor();
        Color bgColor = VisualCommonSettings.getBorderColor();
        PreviewPanel preview = new PreviewPanel(
                "Foreground color", fgColor,
                "Background color", bgColor);

        chooser.setPreviewPanel(preview);
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        dialog = JColorChooser.createDialog(mainWindow, "Pick a Color",
                true, // modal
                chooser, // colour chooser panel
                approx, // OK button handler
                null); // no CANCEL button handler
    }

    private final EventListenerList listenerList = new EventListenerList();
    private final JButton button = new JButton("");

    ColorComboBoxEditor(Color color) {
        button.setBackground(color);
        button.setActionCommand(TAG_EDIT);
        button.addActionListener(this);
    }

    @Override
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    @Override
    public Component getEditorComponent() {
        return button;
    }

    @Override
    public Object getItem() {
        return button.getBackground();
    }

    @Override
    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }

    @Override
    public void selectAll() {
    }

    @Override
    public void setItem(Object value) {
        if (value instanceof Color) {
            button.setBackground((Color) value);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (TAG_EDIT.equals(e.getActionCommand())) {
            // The user has clicked the cell, so bring up the dialog.
            approx.target = this;
            Color color = button.getBackground();
            chooser.setColor(color);
            dialog.setVisible(true);
        } else {
            // User pressed dialog's "OK" button.
            Color color = chooser.getColor();
            button.setBackground(color);
            fireActionEvent(color);
        }
    }

    private void fireActionEvent(Color color) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                ActionEvent actionEvent = new ActionEvent(button, ActionEvent.ACTION_PERFORMED, color.toString());
                ((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
            }
        }
    }

}
