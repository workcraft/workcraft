package org.workcraft.plugins.son.gui;

import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;

public class GranularityPanel extends JPanel{

    private static final long serialVersionUID = 1L;

    private JRadioButton year_yearButton, hour_minsButton;
    private ButtonGroup granularityGroup;

    public GranularityPanel(Border border){
        setLayout(new FlowLayout());
        setBorder(border);

        year_yearButton = new JRadioButton();
        year_yearButton.setText("T:year D:year");
        year_yearButton.setSelected(true);

        hour_minsButton = new JRadioButton();
        hour_minsButton.setText("T:24-hour D:mins");

        granularityGroup = new ButtonGroup();
        granularityGroup.add(year_yearButton);
        granularityGroup.add(hour_minsButton);

        add(year_yearButton);
        add(hour_minsButton);
    }

    public GranularityPanel(){
        this(null);
    }

    public Granularity getSelection(){
        if(year_yearButton.isSelected())
            return Granularity.YEAR_YEAR;
        else if(hour_minsButton.isSelected())
            return Granularity.HOUR_MINS;
        return null;
    }

    public void setSelection(Granularity g){
        if(g == Granularity.YEAR_YEAR){
            year_yearButton.setSelected(true);
        }else if(g == Granularity.HOUR_MINS){
            hour_minsButton.setSelected(true);
        }
    }

    public JRadioButton getYearYearButton() {
        return year_yearButton;
    }

    public JRadioButton getHourMinsButton() {
        return hour_minsButton;
    }
}
