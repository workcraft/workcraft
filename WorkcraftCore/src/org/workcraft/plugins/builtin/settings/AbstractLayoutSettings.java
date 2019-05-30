package org.workcraft.plugins.builtin.settings;

import org.workcraft.gui.properties.Settings;

public abstract class AbstractLayoutSettings implements Settings {

    @Override
    public String getSection() {
        return "Layout";
    }

    @Override
    public String getName() {
        return "Common";
    }

}
