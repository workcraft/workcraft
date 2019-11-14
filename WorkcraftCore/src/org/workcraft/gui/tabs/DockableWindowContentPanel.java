package org.workcraft.gui.tabs;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionButton;
import org.workcraft.gui.actions.ScriptedActionListener;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class DockableWindowContentPanel extends JPanel {

    public static class ViewAction extends Action {
        public static final int CLOSE_ACTION = 1;
        public static final int MINIMIZE_ACTION = 2;
        public static final int MAXIMIZE_ACTION = 3;

        public ViewAction(int windowID, int actionType) {
            super(() -> {
                MainWindow mainWindow = Framework.getInstance().getMainWindow();
                switch (actionType) {
                case CLOSE_ACTION:
                    try {
                        mainWindow.closeDockableWindow(windowID);
                    } catch (OperationCancelledException e) {
                    }
                    break;
                case MAXIMIZE_ACTION:
                    mainWindow.toggleDockableWindowMaximized(windowID);
                    break;
                case MINIMIZE_ACTION:
                    throw new NotSupportedException();
                }
            });
        }
    }

    class DockableViewHeader extends JPanel {
        private ActionButton btnMin;
        private ActionButton btnMax;
        private ActionButton btnClose;
        private final JLabel titleLabel;
        private JPanel buttonPanel = null;
        private boolean maximized = false;

        private ActionButton createHeaderButton(Icon icon, Action action, ScriptedActionListener actionListener) {
            ActionButton button = new ActionButton(action);
            button.addScriptedActionListener(actionListener);
            button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            button.setFocusable(false);
            button.setBorder(null);
            button.setIcon(icon);
            return button;
        }

        DockableViewHeader(String title, int options) {
            super();
            setLayout(new BorderLayout());

            Color c;
            if (UIManager.getLookAndFeel().getName().contains("Substance")) {
                c = getBackground();
                c = new Color((int) (c.getRed() * 0.9), (int) (c.getGreen() * 0.9), (int) (c.getBlue() * 0.9));
            } else {
                c = UIManager.getColor("InternalFrame.activeTitleBackground");
            }
            setBackground(c);

            if  (options != 0) {
                buttonPanel = new JPanel();
                buttonPanel.setBackground(c);
                buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 4, 2));
                buttonPanel.setFocusable(false);
                add(buttonPanel, BorderLayout.EAST);
            }

            int iconCount = 0;
            if ((options & MINIMIZE_BUTTON) != 0) {
                Icon minIcon = UIManager.getIcon("InternalFrame.minimizeIcon");
                ViewAction minAction = new ViewAction(id, ViewAction.MINIMIZE_ACTION);
                btnMin = createHeaderButton(minIcon, minAction, mainWindow.getDefaultActionListener());
                btnMin.setToolTipText("Toggle minimized");
                buttonPanel.add(btnMin);
                iconCount++;
            }

            if ((options & MAXIMIZE_BUTTON) != 0) {
                Icon maxIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
                ViewAction maxAction = new ViewAction(id, ViewAction.MAXIMIZE_ACTION);
                btnMax = createHeaderButton(maxIcon, maxAction, mainWindow.getDefaultActionListener());
                buttonPanel.add(btnMax);
                iconCount++;
            }

            Icon closeIcon = UIManager.getIcon("InternalFrame.closeIcon");
            if ((options & CLOSE_BUTTON) != 0) {
                ViewAction closeAction = new ViewAction(id, ViewAction.CLOSE_ACTION);
                btnClose = createHeaderButton(closeIcon, closeAction, mainWindow.getDefaultActionListener());
                btnClose.setToolTipText("Close window");
                buttonPanel.add(btnClose);
                iconCount++;
            }

            if (iconCount != 0) {
                Dimension size = new Dimension((closeIcon.getIconWidth() + 4) * iconCount, closeIcon.getIconHeight() + 4);
                buttonPanel.setPreferredSize(size);
            }

            titleLabel = new JLabel(title);
            titleLabel.setOpaque(false);
            titleLabel.setForeground(UIManager.getColor("InternalFrame.activeTitleForeground"));
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            titleLabel.setBorder(SizeHelper.getEmptyBorder());
            add(titleLabel, BorderLayout.WEST);

            setMaximized(false);
        }

        public boolean isMaximized() {
            return maximized;
        }

        public void setMaximized(boolean maximized) {
            this.maximized = maximized;

            if (btnMax != null) {
                if (maximized) {
                    btnMax.setIcon(UIManager.getIcon("InternalFrame.minimizeIcon"));
                    btnMax.setToolTipText("Restore window");
                } else {
                    btnMax.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
                    btnMax.setToolTipText("Maximize window");
                }
            }
        }

        public void setTitle(String title) {
            titleLabel.setText(title);
            titleLabel.repaint();
        }
    }

    public static final int CLOSE_BUTTON = 1;
    public static final int MINIMIZE_BUTTON = 2;
    public static final int MAXIMIZE_BUTTON = 4;
    public static final int HEADER = 8;

    private String title;
    private final JComponent content;
    private final JPanel contentPane;
    public final DockableViewHeader header;
    private final MainWindow mainWindow;
    private final int id;
    private int options;

    public DockableWindowContentPanel(MainWindow mainWindow,
            int id, String title, JComponent content, int options) {

        super();
        setLayout(new BorderLayout(0, 0));

        this.title = title;
        this.mainWindow = mainWindow;
        this.id = id;
        this.content = content;

        if ((options & ~HEADER) > 0) {
            this.options = options | HEADER;
        } else {
            this.options = options;
        }
        header = new DockableViewHeader(title, options);

        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.add(content, BorderLayout.CENTER);
        contentPane.setBorder(new LineBorder(contentPane.getBackground(), SizeHelper.getBorderThickness()));

        if ((options & HEADER) > 0) {
            contentPane.add(header, BorderLayout.NORTH);
        }
        add(contentPane, BorderLayout.CENTER);
        setFocusable(false);
    }

    public boolean isMaximized() {
        if (header != null) {
            return header.isMaximized();
        } else {
            return false;
        }
    }

    public void setMaximized(boolean maximized) {
        if (header != null) {
            header.setMaximized(maximized);
        }
    }

    public void setHeaderVisible(boolean headerVisible) {
        if (headerVisible && ((options & HEADER) > 0)) {
            if (header.getParent() != contentPane) {
                contentPane.add(header, BorderLayout.NORTH);
            }
        } else {
            contentPane.remove(header);
        }
        contentPane.doLayout();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        header.setTitle(title);
    }

    public int getID() {
        return id;
    }

    public JComponent getContent() {
        return content;
    }

    public int getOptions() {
        return options;
    }

}
