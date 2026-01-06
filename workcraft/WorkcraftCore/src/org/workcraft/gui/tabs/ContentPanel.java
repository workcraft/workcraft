package org.workcraft.gui.tabs;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionButton;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ContentPanel extends JPanel {

    class DockableViewHeader extends JPanel {
        private ActionButton btnMax = null;
        private final JLabel titleLabel;
        private boolean maximized = false;

        private ActionButton createHeaderButton(Icon icon, Action action) {
            ActionButton button = new ActionButton(null, action);
            button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            button.setFocusable(false);
            button.setBorder(null);
            button.setIcon(icon);
            return button;
        }

        DockableViewHeader() {
            super();
            setLayout(new BorderLayout());

            Color color = getTitleBackgroundColor();
            setBackground(color);

            JPanel buttonPanel = null;
            if  (options != 0) {
                buttonPanel = new JPanel();
                buttonPanel.setBackground(color);
                buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 4, 2));
                buttonPanel.setFocusable(false);
                add(buttonPanel, BorderLayout.EAST);
            }

            final MainWindow mainWindow = Framework.getInstance().getMainWindow();
            int iconCount = 0;

            if ((options & MAXIMIZE_BUTTON) != 0) {
                btnMax = createHeaderButton(UIManager.getIcon("InternalFrame.maximizeIcon"),
                        new Action(null, () -> mainWindow.togglePanelDockableMaximized(panelDockable),
                                "Toggle maximised"));

                buttonPanel.add(btnMax);
                iconCount++;
            }

            Icon closeIcon = UIManager.getIcon("InternalFrame.closeIcon");
            if ((options & CLOSE_BUTTON) != 0) {
                ActionButton btnClose = createHeaderButton(closeIcon,
                        new Action(null, () -> mainWindow.closePanelDockable(panelDockable),
                                "Close window"));

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
            titleLabel.setBorder(GuiUtils.getEmptyBorder());
            add(titleLabel, BorderLayout.WEST);

            setMaximized(false);
        }

        private Color getTitleBackgroundColor() {
            if (UIManager.getLookAndFeel().getName().contains("Substance")) {
                return ColorUtils.fade(getBackground(), 0.9);
            } else {
                return UIManager.getColor("InternalFrame.activeTitleBackground");
            }
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
    public static final int MAXIMIZE_BUTTON = 4;
    public static final int HEADER = 8;

    private String title;
    private final JComponent content;
    private final JPanel contentPane;
    private final DockableViewHeader header;
    private final int options;
    private PanelDockable panelDockable;

    public ContentPanel(String title, JComponent content, int options) {
        super();
        setLayout(new BorderLayout(0, 0));

        this.title = title;
        this.content = content;

        if ((options & ~HEADER) > 0) {
            this.options = options | HEADER;
        } else {
            this.options = options;
        }
        header = new DockableViewHeader();

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

    public void setMaximized(boolean value) {
        if (header != null) {
            header.setMaximized(value);
        }
    }

    public void setHeaderVisible(boolean value) {
        if (value && ((options & HEADER) > 0)) {
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

    public void setTitle(String value) {
        title = value;
        header.setTitle(value);
    }

    public Component getHeader() {
        return header;
    }

    public JComponent getContent() {
        return content;
    }

    public int getOptions() {
        return options;
    }

    public PanelDockable getPanelDockable() {
        return panelDockable;
    }

    public void setDockableWindow(PanelDockable panelDockable) {
        this.panelDockable = panelDockable;
    }

}
