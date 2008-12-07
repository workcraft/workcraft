package org.workcraft.gui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public abstract class InternalWindow extends JInternalFrame {
	private static final long serialVersionUID = 1L;

	protected Dimension titleRestore = null;
	protected Border borderRestore = null;

	public InternalWindow(String title) {
		super(title, true, true, true, true);
		this.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
	}

	public void hideTitle() {
	//	System.out.println ("hiding title " +this.getTitle());
		JComponent titlePane = ((BasicInternalFrameUI)getUI()).getNorthPane();
		titleRestore =  titlePane.getPreferredSize();
		titlePane.setPreferredSize(new Dimension(0,0));
	}

	public void hideBorder() {
	//	System.out.println ("hiding border " +this.getTitle());
		borderRestore = getBorder();
		setBorder(null);
	}

	public void showTitle() {
		if (titleRestore != null) {
			JComponent titlePane = ((BasicInternalFrameUI)getUI()).getNorthPane();
			titlePane.setPreferredSize(titleRestore);
			titleRestore = null;
		}
	}

	public void showBorder() {
		if (borderRestore != null) {
			setBorder(borderRestore);
			borderRestore = null;
		}
	}
}
