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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.workcraft.Framework;
import org.workcraft.gui.SmartFlowLayout;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.TaskMonitor;

@SuppressWarnings("serial")
public class TaskManagerWindow extends JPanel implements TaskMonitor {
	class TaskControlMonitor implements ProgressMonitor {
		TaskManagerWindow window;
		TaskControl taskControl;

		public TaskControlMonitor (TaskManagerWindow window, TaskControl taskControl) {
			this.taskControl = taskControl;
			this.window = window;
		}

		@Override
		public boolean isCancelRequested() {
			return taskControl.isCancelRequested();
		}

		@Override
		public void finished(Result result, String description) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.removeTaskControl(taskControl);
				}
			});
		}

		@Override
		public void logMessage(String message) {
			System.out.println(message);
		}

		@Override
		public void logErrorMessage(String message) {
			System.err.println (message);
		}

		@Override
		public void progressUpdate(final double completion) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					taskControl.progressUpdate(completion);
				}
			});
		}
	}

	public class ScrollPaneWidthTrackingPanel extends JPanel implements Scrollable {
	    private static final long serialVersionUID = 1L;

	    public ScrollPaneWidthTrackingPanel() {
	        super();
	    }

	    public Dimension getPreferredScrollableViewportSize() {
	        return getPreferredSize();
	    }

	    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
	        return Math.max(visibleRect.height * 9 / 10, 1);
	    }

	    public boolean getScrollableTracksViewportHeight() {
	        return false;
	    }

	    public boolean getScrollableTracksViewportWidth() {
	        return true;
	    }

	    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
	        return Math.max(visibleRect.height / 10, 1);
	    }
	}

	static class TaskControlGenerator implements Runnable {
		private TaskControl taskControl;
		private Framework framework;
		private JPanel container;
		private String description;

		public TaskControlGenerator(Framework framework, JPanel container,
				String description) {
			this.framework = framework;
			this.container = container;
			this.description = description;
		}

		public TaskControl getTaskCotnrol() {
			return taskControl;
		}

		@Override
		public void run() {
			taskControl = new TaskControl(framework, description);
			container.add(taskControl);
			container.revalidate();
		}

	};

	private Framework framework;
	private int counter = 0;

	private JScrollPane scroll;
	private JPanel content;

	public TaskManagerWindow (final Framework framework) {
		this.framework = framework;

		setLayout(new BorderLayout());
		scroll = new JScrollPane();
		add (scroll, BorderLayout.CENTER);

		content = new ScrollPaneWidthTrackingPanel();
		//ytcontent.setBackground(Color.ORANGE);
		content.setLayout(new SmartFlowLayout());

		scroll.setViewportView(content);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		Border lineBorder = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setBorder(lineBorder);

		framework.getTaskManager().addObserver(this);

		JButton comp = new JButton("Queue test task");

		comp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				framework.getTaskManager().queue(new Task(){
					@Override
					public Result run(ProgressMonitor monitor) {
						for (int i=0; i < 100; i++) {
							try {
								if (monitor.isCancelRequested()) {
									return Result.CANCELLED;
								}
								Thread.sleep((int)(Math.random()*100+20));
							} catch (InterruptedException e) {
								return Result.FAILED;
							}
							monitor.progressUpdate(i/99.0);
						}
						return Result.OK;
					} }, "Test task #" + counter++, new DummyProgressMonitor(){

						@Override
						public void finished(Result result, final String description) {
							if (result.getExitStatus() == Result.ExitStatus.OK )
							{
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										JOptionPane.showMessageDialog(null, "Task " + description + " finished!");
									}

								});
							}
						}
					});
			}
		});

		content.add (comp);
	}

	public void removeTaskControl (TaskControl taskControl) {
		content.remove(taskControl);
		content.revalidate();
	}

	@Override
	public ProgressMonitor taskStarting(final String description) {
		TaskControlGenerator tcg = new TaskControlGenerator(framework, content, description);
		try {
			SwingUtilities.invokeAndWait(tcg);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return new TaskControlMonitor (this, tcg.getTaskCotnrol());
	}
}
