package org.workcraft.gui.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class FileCellRenderer extends JPanel implements TableCellRenderer {
	    Border unselectedBorder = null;
	    Border selectedBorder = null;

		final private JButton chooseButton;
		final private JButton clearButton;

	    public FileCellRenderer() {
			chooseButton = new JButton();
			chooseButton.setBorderPainted(false);
			chooseButton.setFocusable(false);
			chooseButton.setOpaque(true);
			chooseButton.setMargin(new Insets(1, 1, 1, 1));
			chooseButton.setHorizontalAlignment(SwingConstants.LEFT);

			clearButton = new JButton("x");
			clearButton.setFocusable(false);
			clearButton.setMargin(new Insets(1, 1, 1, 1));

	    	setLayout(new BorderLayout());
	    	add(chooseButton, BorderLayout.CENTER);
	    	add(clearButton, BorderLayout.EAST);
	        setFocusable(false);
	    }

	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value,
	    		boolean isSelected, boolean hasFocus, int row, int column) {

	    	chooseButton.setFont(table.getFont());
	    	if (value == null) {
	    		chooseButton.setText("");
	    	} else {
	    		File file = (File)value;
	    		if (file.exists()) {
	    			chooseButton.setForeground(Color.BLACK);
	    		} else {
	    			chooseButton.setForeground(Color.RED);
	    		}
	    		chooseButton.setText(".../" + file.getName());

	    		if (isSelected) {
	    			if (selectedBorder == null) {
	    				selectedBorder = BorderFactory.createMatteBorder(
	    						0, 0, 0, 0,	table.getSelectionBackground());
	    			}
	    			setBorder(selectedBorder);
	    		} else {
	    			if (unselectedBorder == null) {
	    				unselectedBorder = BorderFactory.createMatteBorder(
	    						0, 0, 0, 0, table.getBackground());
	    			}
	    			setBorder(unselectedBorder);
	    		}
	    	}
	        return this;
	    }
}
