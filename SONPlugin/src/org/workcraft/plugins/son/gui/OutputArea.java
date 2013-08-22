package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class OutputArea extends JFrame {

	private static final long serialVersionUID = 1L;
	private Font font = new Font("Calibri", Font.PLAIN, 15);

	public OutputArea(JTextArea textArea) {

        textArea.setEditable(false);
        textArea.setFont(font);

        Container contentPane = this.getContentPane ();

        contentPane.setLayout (new BorderLayout ());
        contentPane.add (
            new JScrollPane (
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            BorderLayout.CENTER);
        this.pack ();
        this.setVisible (true);
        this.setTitle("Verification Result");

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize=kit.getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        setLocation(width/2,height/4);
    }

}