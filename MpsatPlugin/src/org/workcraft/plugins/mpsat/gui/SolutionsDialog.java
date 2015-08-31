package org.workcraft.plugins.mpsat.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;


@SuppressWarnings("serial")
public class SolutionsDialog extends JDialog {
	private JPanel contents;
	private JPanel solutionsPanel;
	private JPanel buttonsPanel;

	public SolutionsDialog(MpsatChainTask task, String title, String message, List<Solution> solutions) {

		double sizes[][] = {
				{ TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED }
		};


		solutionsPanel = new JPanel();
		solutionsPanel.setLayout(new BoxLayout(solutionsPanel, BoxLayout.Y_AXIS));
		for (Solution solution : solutions) {
			solutionsPanel.add(new SolutionPanel(task, solution, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SolutionsDialog.this.setVisible(false);
				}
			}));
		}

		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SolutionsDialog.this.setVisible(false);
			}
		});
		buttonsPanel.add(okButton);

		contents = new JPanel(new TableLayout(sizes));
		contents.add(new JLabel(message), "0 0");
		contents.add(solutionsPanel, "0 1");
		contents.add(buttonsPanel, "0 2");

		this.setTitle(title);
		this.setContentPane(contents);
		setMinimumSize(new Dimension(450, 200));
		setSize(new Dimension(500, 300));
		this.setModal(true);
	}

}
