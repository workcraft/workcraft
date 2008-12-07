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
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
	}

	public void hideTitle() {
		//	System.out.println ("hiding title " +this.getTitle());
		JComponent titlePane = ((BasicInternalFrameUI)getUI()).getNorthPane();
		this.titleRestore =  titlePane.getPreferredSize();
		titlePane.setPreferredSize(new Dimension(0,0));
	}

	public void hideBorder() {
		//	System.out.println ("hiding border " +this.getTitle());
		this.borderRestore = getBorder();
		setBorder(null);
	}

	public void showTitle() {
		if (this.titleRestore != null) {
			JComponent titlePane = ((BasicInternalFrameUI)getUI()).getNorthPane();
			titlePane.setPreferredSize(this.titleRestore);
			this.titleRestore = null;
		}
	}

	public void showBorder() {
		if (this.borderRestore != null) {
			setBorder(this.borderRestore);
			this.borderRestore = null;
		}
	}
}
