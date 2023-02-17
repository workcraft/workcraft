package org.workcraft.gui.tabs;

import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.DefaultDockingStrategy;
import org.flexdock.docking.defaults.StandardBorderManager;
import org.flexdock.plaf.common.border.ShadowBorder;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class ScrollDockingPort extends DefaultDockingPort {

    private static final String FLEXDOCK_DOCKING_PORT = "defaultDockingPort";

    private static class ScrollDockingStrategy extends DefaultDockingStrategy {
        @Override
        protected ScrollDockingPort createDockingPortImpl(DockingPort base) {
            return new ScrollDockingPort();
        }
    }

    static {
        initStatic();
    }

    private static void initStatic() {
        DockingManager.setDockingStrategy(ScrollDockingPort.class, new ScrollDockingStrategy());
    }

    public ScrollDockingPort() {
        super(FLEXDOCK_DOCKING_PORT);
        setBorderManager(new StandardBorderManager(new ShadowBorder()));
    }

    @Override
    protected JTabbedPane createTabbedPane() {
        JTabbedPane tabbedPane = super.createTabbedPane();
        tabbedPane.setUI(new DockingTabbedPaneUI());
        switch (EditorCommonSettings.getTabStyle()) {
        case WRAP:
            tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
            break;
        case SCROLL:
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            break;
        }
        return tabbedPane;
    }

    private static class DockingTabbedPaneUI extends BasicTabbedPaneUI {

        @Override
        protected JButton createScrollButton(int direction) {
            return new DockingArrowButton(direction);
        }
    }

    private static class DockingArrowButton extends BasicArrowButton implements UIResource {

        DockingArrowButton(int direction) {
            super(direction,
                    UIManager.getColor("TabbedPane.selected"),
                    UIManager.getColor("TabbedPane.shadow"),
                    UIManager.getColor("TabbedPane.darkShadow"),
                    UIManager.getColor("TabbedPane.highlight"));
        }

        @Override
        public Dimension getPreferredSize() {
            int d = SizeHelper.getTabScrollButtonSize();
            return new Dimension(d, d);
        }
    }

}
