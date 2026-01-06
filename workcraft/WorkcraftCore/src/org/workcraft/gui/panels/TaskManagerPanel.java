package org.workcraft.gui.panels;

import org.workcraft.Framework;
import org.workcraft.gui.layouts.SmartFlowLayout;
import org.workcraft.gui.tasks.TaskControl;
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

public class TaskManagerPanel extends JPanel implements TaskMonitor {

    static class TaskControlMonitor extends BasicProgressMonitor<Object> {
        private final TaskManagerPanel taskManagerPanel;
        private final TaskControl taskControl;

        TaskControlMonitor(TaskManagerPanel taskManagerPanel, TaskControl taskControl) {
            this.taskManagerPanel = taskManagerPanel;
            this.taskControl = taskControl;
        }

        @Override
        public boolean isCancelRequested() {
            return taskControl.isCancelRequested();
        }

        @Override
        public void isFinished(Result<?> result) {
            super.isFinished(result);
            SwingUtilities.invokeLater(() -> taskManagerPanel.removeTaskControl(taskControl));
        }

        @Override
        public void progressUpdate(final double completion) {
            SwingUtilities.invokeLater(() -> taskControl.setProgress(completion));
        }

        @Override
        public void setDetails(String details) {
            SwingUtilities.invokeLater(() -> taskControl.setDetails(details));
        }
    }

    public static class ScrollPaneWidthTrackingPanel extends JPanel implements Scrollable {
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

        public TaskControl getTaskControl() {
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
    private final Runnable updater;

    public TaskManagerPanel(Runnable updater) {
        this.updater = updater;
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

        Framework.getInstance().getTaskManager().addObserver(this);
    }

    public void removeTaskControl(TaskControl taskControl) {
        content.remove(taskControl);
        content.revalidate();
        updater.run();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ProgressMonitor<?> taskStarting(final String description) {
        TaskControlGenerator tcg = new TaskControlGenerator(content, description);
        updater.run();
        if (SwingUtilities.isEventDispatchThread()) {
            tcg.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(tcg);
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return new TaskControlMonitor(this, tcg.getTaskControl());
    }

    public boolean isEmpty() {
        return content.getComponentCount() <= 0;
    }

}
