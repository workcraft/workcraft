package org.workcraft.plugins.son.gui;

import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;

import javax.swing.*;
import javax.swing.border.Border;

public class GranularityPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JRadioButton yearYearButton;
    private final JRadioButton hourMinsButton;

    public GranularityPanel(Border border) {
        setLayout(new WrapLayout());
        setBorder(border);

        yearYearButton = new JRadioButton();
        yearYearButton.setText("T:year D:year");
        yearYearButton.setSelected(true);

        hourMinsButton = new JRadioButton();
        hourMinsButton.setText("T:24-hour D:mins");

        ButtonGroup granularityGroup = new ButtonGroup();
        granularityGroup.add(yearYearButton);
        granularityGroup.add(hourMinsButton);

        add(yearYearButton);
        add(hourMinsButton);
    }

    public Granularity getSelection() {
        if (yearYearButton.isSelected()) {
            return Granularity.YEAR_YEAR;
        } else if (hourMinsButton.isSelected()) {
            return Granularity.HOUR_MINS;
        }
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
