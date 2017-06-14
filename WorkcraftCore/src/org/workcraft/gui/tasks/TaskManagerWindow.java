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
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.layouts.SmartFlowLayout;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.TaskMonitor;

@SuppressWarnings("serial")
public class TaskManagerWindow extends JPanel implements TaskMonitor {
    class TaskControlMonitor implements ProgressMonitor<Object> {
        TaskManagerWindow window;
        TaskControl taskControl;

        TaskControlMonitor(TaskManagerWindow window, TaskControl taskControl) {
            this.taskControl = taskControl;
            this.window = window;
        }

        @Override
        public boolean isCancelRequested() {
            return taskControl.isCancelRequested();
        }

        @Override
        public void finished(Result<? extends Object> result, String description) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window.removeTaskControl(taskControl);
                }
            });
        }

        @Override
        public void stdout(byte[] data) {
        }

        @Override
        public void stderr(byte[] data) {
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
        private final JPanel container;
        private final String description;

        TaskControlGenerator(JPanel container, String description) {
            this.container = container;
            this.description = description;
        }

        public TaskControl getTaskCotnrol() {
            return taskControl;
        }

        @Override
        public void run() {
            taskControl = new TaskControl(description);
            container.add(taskControl);
            container.revalidate();
        }

    }

    private int counter = 0;

    private final JPanel content;

    public TaskManagerWindow() {

        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane();
        add(scroll, BorderLayout.CENTER);

        content = new ScrollPaneWidthTrackingPanel();
        //ytcontent.setBackground(Color.ORANGE);
        content.setLayout(new SmartFlowLayout());

        scroll.setViewportView(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        Border outsideBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        Border insideBorder = SizeHelper.getEmptyBorder();
        Border lineBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
        setBorder(lineBorder);

        final Framework framework = Framework.getInstance();
        framework.getTaskManager().addObserver(this);

        // Do we really need this "Queue test task" button? Probably not.
        // addQueueTestTasksButton(framework);

    }

    private void addQueueTestTasksButton(final Framework framework) {
        JButton testTaskButton = new JButton("Queue test task");

        testTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                framework.getTaskManager().queue(new Task<Object>() {
                    @Override
                    public Result<Object> run(ProgressMonitor<Object> monitor) {
                        for (int i = 0; i < 100; i++) {
                            try {
                                if (monitor.isCancelRequested()) {
                                    return new Result<Object>(Outcome.CANCELLED);
                                }
                                Thread.sleep((int) (Math.random() * 100 + 20));
                            } catch (InterruptedException e) {
                                return new Result<Object>(Outcome.FAILED);
                            }
                            monitor.progressUpdate(i / 99.0);
                        }
                        return new Result<Object>(Outcome.FINISHED);
                    }
                }, "Test task #" + counter++, new DummyProgressMonitor<Object>() {

                        @Override
                        public void finished(Result<? extends Object> result, final String description) {
                            if (result.getOutcome() == Outcome.FINISHED) {
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

        content.add(testTaskButton);
    }

    public void removeTaskControl(TaskControl taskControl) {
        content.remove(taskControl);
        content.revalidate();
    }

    @Override
    public ProgressMonitor<Object> taskStarting(final String description) {
        TaskControlGenerator tcg = new TaskControlGenerator(content, description);
        if (SwingUtilities.isEventDispatchThread()) {
            tcg.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(tcg);
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return new TaskControlMonitor(this, tcg.getTaskCotnrol());
    }
}
