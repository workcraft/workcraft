package org.workcraft.plugins.mpsat.gui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class MpsatHelpPage extends JDialog {

	public MpsatHelpPage(Window owner, String url) {
		super(owner, "MPSat help", ModalityType.APPLICATION_MODAL);

	    try {
			JEditorPane htmlPane = new JEditorPane();
			htmlPane.setPage(new URL("file://" + System.getProperty("user.dir") + "/" + url));
			htmlPane.setEditable(false);
			add(new JScrollPane(htmlPane), BorderLayout.CENTER);

		    getRootPane().registerKeyboardAction(new ActionListener() {
		    	@Override
		    	public void actionPerformed(ActionEvent e) {
		    		setVisible(false);
		    	}
		    },
		    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
		    JComponent.WHEN_IN_FOCUSED_WINDOW);
		} catch(IOException ioe) {
		    System.out.println("Cannot display HTML page " + url + ": " + ioe);
		}

	}
}
