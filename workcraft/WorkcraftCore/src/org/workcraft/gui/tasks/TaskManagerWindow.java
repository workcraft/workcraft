package org.workcraft.gui.tasks;

import org.workcraft.Framework;
import org.workcraft.gui.layouts.SmartFlowLayout;
import org.workcraft.tasks.BasicProgressMonitor;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskMonitor;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("serial")
public class TaskManagerWindow extends JPanel implements TaskMonitor {

    class TaskControlMonitor extends BasicProgressMonitor<Object> {
        private final TaskManagerWindow window;
        private final TaskControl taskControl;

        TaskControlMonitor(TaskManagerWindow window, TaskControl taskControl) {
            this.taskControl = taskControl;
            this.window = window;
        }

        @Override
        public boolean isCancelRequested() {
            return taskControl.isCancelRequested();
        }

        @Override
        public void isFinished(Result<? extends Object> result) {
            super.isFinished(result);
            SwingUtilities.invokeLater(() -> window.removeTaskControl(taskControl));
        }

        @Override
        public void progressUpdate(final double completion) {
            SwingUtilities.invokeLater(() -> taskControl.progressUpdate(completion));
        }
    }

    public class ScrollPaneWidthTrackingPanel extends JPanel implements Scrollable {
        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height * 9 / 10, 1);
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
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

        Border outsideBorder = new LineBorder(Color.LIGHT_GRAY);
        Border insideBorder = GuiUtils.getEmptyBorder();
        Border lineBorder = new CompoundBorder(outsideBorder, insideBorder);
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
        SwingUtilities.invokeLater(() -> {
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
        });
    }

}
