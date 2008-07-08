package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class MDIDocumentFrame extends JInternalFrame implements InternalFrameListener {
	private static final long serialVersionUID = 1L;

	protected MDIPane ownerMDIPane = null;
	protected JTextArea content = null;

	protected Dimension titleRestore = null;
	protected Border borderRestore = null;

	public MDIDocumentFrame(String title) {
		super(title, true, true, true, true);
		content = new JTextArea();
		content.setWrapStyleWord(true);
		content.setLineWrap(true);
		this.setLayout(new BorderLayout());
		this.add(content, BorderLayout.CENTER);



		this.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		this.addInternalFrameListener(this);
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


	public void internalFrameActivated(InternalFrameEvent e) {
	}

	public void internalFrameClosed(InternalFrameEvent e) {
	}

	public void internalFrameClosing(InternalFrameEvent e) {
		if (ownerMDIPane != null)
			ownerMDIPane.handleClosing(this, -1);
		else
			this.dispose();
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
	}

	public void internalFrameDeiconified(InternalFrameEvent e) {
	}

	public void internalFrameIconified(InternalFrameEvent e) {
	}

	public void internalFrameOpened(InternalFrameEvent e) {
	}
}
