package org.workcraft.gui.tasks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.layouts.SmartFlowLayout;
import org.workcraft.tasks.BasicProgressMonitor;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskMonitor;

@SuppressWarnings("serial")
public class TaskManagerWindow extends JPanel implements TaskMonitor {

    class TaskControlMonitor extends BasicProgressMonitor<Object> {
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
        public void finished(Result<? extends Object> result) {
            super.finished(result);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    window.removeTaskControl(taskControl);
                }
            });
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

    private final JPanel content;

    public TaskManagerWindow() {
        setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane();
        add(scroll, BorderLayout.CENTER);

        content = new ScrollPaneWidthTrackingPanel();
        content.setLayout(new SmartFlowLayout());

        scroll.setViewportView(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        Border outsideBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        Border insideBorder = SizeHelper.getEmptyBorder();
        Border lineBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
        setBorder(lineBorder);

        final Framework framework = Framework.getInstance();
        framework.getTaskManager().addObserver(this);
    }

    public void removeTaskControl(TaskControl taskControl) {
        content.remove(taskControl);
        content.revalidate();
        if (content.getComponentCount() == 0) {
            setTabActivity(false);
        }
    }

    @Override
    public ProgressMonitor<Object> taskStarting(final String description) {
        TaskControlGenerator tcg = new TaskControlGenerator(content, description);

        setTabActivity(true);

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

    private void setTabActivity(final boolean active) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Container component = getParent().getParent();
                Container parent = component.getParent();
                if (parent instanceof JTabbedPane) {
                    JTabbedPane tab = (JTabbedPane) parent;
                    for (int i = 0; i < tab.getTabCount(); i++) {
                        if (tab.getComponentAt(i) != component) continue;

                        Component tabComponent = tab.getTabComponentAt(i);
                        if (active) {
                            tabComponent.setForeground(new Color(0.22f, 0.45f, 0.9f));
                        } else {
                            tabComponent.setForeground(Color.BLACK);
                        }
                    }
                }
            }
        });
    }

}
