package org.workcraft.gui;

import javax.swing.JInternalFrame;

public class InternalFloaterWindow extends JInternalFrame {
	private static final long serialVersionUID = 1L;

	public InternalFloaterWindow(String title) {
		super (title, false, false, false, true);
		putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
	}

}
