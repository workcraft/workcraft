package org.workcraft.gui;

import javax.swing.JInternalFrame;

public class MDIFloaterFrame extends JInternalFrame {
	private static final long serialVersionUID = 1L;

	public MDIFloaterFrame(String title) {
		super (title, false, false, false, true);
		this.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
	}

}
