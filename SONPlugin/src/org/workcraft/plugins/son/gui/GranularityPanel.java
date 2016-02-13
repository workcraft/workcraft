package org.workcraft.plugins.son.gui;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;

public class GranularityPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JRadioButton yearYearButton, hourMinsButton;
    private ButtonGroup granularityGroup;

    public GranularityPanel(Border border) {
        setLayout(new FlowLayout());
        setBorder(border);

        yearYearButton = new JRadioButton();
        yearYearButton.setText("T:year D:year");
        yearYearButton.setSelected(true);

        hourMinsButton = new JRadioButton();
        hourMinsButton.setText("T:24-hour D:mins");

        granularityGroup = new ButtonGroup();
        granularityGroup.add(yearYearButton);
        granularityGroup.add(hourMinsButton);

        add(yearYearButton);
        add(hourMinsButton);
    }

    public GranularityPanel() {
        this(null);
    }

    public Granularity getSelection() {
        if (yearYearButton.isSelected())
            return Granularity.YEAR_YEAR;
        else if (hourMinsButton.isSelected())
            return Granularity.HOUR_MINS;
        return null;
    }

    public void setSelection(Granularity g) {
        if (g == Granularity.YEAR_YEAR) {
            yearYearButton.setSelected(true);
        } else if (g == Granularity.HOUR_MINS) {
            hourMinsButton.setSelected(true);
        }
    }

    public JRadioButton getYearYearButton() {
        return yearYearButton;
    }

    public JRadioButton getHourMinsButton() {
        return hourMinsButton;
    }
}
