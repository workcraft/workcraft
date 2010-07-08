package org.workcraft.plugins.verification.gui;

import info.clearthought.layout.TableLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.Trace;


@SuppressWarnings("serial")
public class SolutionPanel extends JPanel {
	private JPanel buttonsPanel;
	private final Trace trace;
	private JTextArea traceText;

	public SolutionPanel(Trace t) {
		super (new TableLayout(new double[][]
		                                    { { TableLayout.FILL, TableLayout.PREFERRED },
				{TableLayout.FILL} }
		));

		this.trace = t;

		traceText = new JTextArea();
		traceText.setText(t.toString());

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(traceText);

		buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

		JButton saveButton = new JButton("Save");
		JButton playButton = new JButton("Play trace");

		buttonsPanel.add(saveButton);
		buttonsPanel.add(playButton);


		add(scrollPane, "0 0");
		add(buttonsPanel, "1 0");
	}
}
