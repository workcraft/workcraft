package org.workcraft.gui.tabs;

import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.DefaultDockingStrategy;
import org.flexdock.docking.defaults.StandardBorderManager;
import org.flexdock.plaf.common.border.ShadowBorder;

import javax.swing.*;

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
        JTabbedPane tabbed = super.createTabbedPane();
        tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        return tabbed;
    }

}
