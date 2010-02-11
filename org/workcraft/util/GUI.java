package org.workcraft.util;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GUI {
		public static JPanel createLabeledComponent (JComponent component, String labelText) {
		JPanel result = new JPanel (new FlowLayout(FlowLayout.LEFT, 3, 0));
		result.add(new JLabel(labelText));
		result.add(component);
		return result;
	}

	public static void centerFrameToParent(Window frame, Window parent) {
		Dimension parentSize = parent.getSize();
		frame.setSize(parentSize.width / 2, parentSize.height / 2);
		Dimension mySize = frame.getSize();
		parent.getLocationOnScreen();

		frame.setLocation (((parentSize.width - mySize.width)/2) + 0, ((parentSize.height - mySize.height)/2) + 0);
	}
}
