package org.workcraft.plugins.builtin.settings;

import org.workcraft.gui.properties.Settings;

public abstract class AbstractHiddenSettings implements Settings {

    @Override
    public String getSection() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

}
