package org.workcraft.plugins.builtin.settings;

import org.workcraft.gui.properties.Settings;

public abstract class AbstractToolSettings implements Settings {

    @Override
    public String getSection() {
        return "External tools";
    }

}
