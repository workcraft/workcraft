package org.workcraft.gui;

import javax.swing.JPanel;

import org.workcraft.gui.graph.EditorOverlay;

@SuppressWarnings("serial")
public class Overlay extends JPanel implements EditorOverlay {

	public Overlay() {
		super(null);
		setOpaque(false);
	}
}
