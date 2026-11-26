package org.workcraft.gui.properties;

import org.workcraft.Config;

public interface Settings extends Properties {
    void save(Config config);
    void load(Config config);

    /**
     * Called when settings values become current: loaded from config or confirmed in the settings dialog.
     */
    default void apply() {
    }

    String getSection();
    String getName();
}
