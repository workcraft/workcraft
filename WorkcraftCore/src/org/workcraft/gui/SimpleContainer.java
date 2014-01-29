package org.workcraft.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class SimpleContainer extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JPanel emptyPanel;
	private JPanel activePanel;

	public SimpleContainer() {
		emptyPanel = new DisabledPanel();

		setLayout(new BorderLayout(2, 2));

		add(emptyPanel, BorderLayout.CENTER);
		activePanel = emptyPanel;
	}

	public void setContent(JPanel panel) {
		remove(activePanel);
		JPanel newPanel = panel!=null?panel:emptyPanel;
		add(newPanel, BorderLayout.CENTER);
		activePanel = newPanel;
		doLayout();
		repaint();
	}
}
