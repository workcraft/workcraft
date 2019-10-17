package org.workcraft.plugins.builtin.settings;

import org.workcraft.gui.properties.Settings;

public abstract class AbstractModelSettings implements Settings {

    @Override
    public String getSection() {
        return "Models";
    }

}
