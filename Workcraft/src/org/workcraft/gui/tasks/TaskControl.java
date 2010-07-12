/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.gui.tasks;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import org.workcraft.Framework;

@SuppressWarnings("serial")
public class TaskControl extends JPanel {
	JLabel label;
	JProgressBar progressBar;
	JButton btnCancel;
	JButton btnLog;

	volatile boolean cancelRequested;

	public TaskControl (Framework framework, String taskDescription) {
		double size[][] = {
				{TableLayout.FILL, 80, 100},
				{20, 20, 20}
		};

		Border lineBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(3, 3, 3, 3));

		setBorder(lineBorder);

		TableLayout layout = new TableLayout(size);
		layout.setHGap(3);
		layout.setVGap(3);
		setLayout(layout);

		label = new JLabel(taskDescription);
		label.setMinimumSize(new Dimension(100,20));
		label.setPreferredSize(new Dimension(300,20));

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setMinimum(0);
		progressBar.setMaximum(1000);

		progressBar.setMinimumSize(new Dimension (100,20));
		progressBar.setPreferredSize(new Dimension (300,20));

		btnLog = new JButton("Show log");
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelRequested = true;
				btnCancel.setEnabled(false);
				btnCancel.setText("Cancelling...");
			}
		});

		add(label, "0,0,2,0");
		add(progressBar, "0,1,2,1" );
		add(btnLog, "1,2");
		add(btnCancel, "2,2");
	}

	public void progressUpdate(final double completion) {
		progressBar.setIndeterminate(false);
		progressBar.setValue((int)(completion * 1000));
	}

	public boolean isCancelRequested() {
		return cancelRequested;
	}
}