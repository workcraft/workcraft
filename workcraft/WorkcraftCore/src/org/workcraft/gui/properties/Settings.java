package org.workcraft.gui.properties;

import org.workcraft.Config;

public interface Settings extends Properties {
    void save(Config config);
    void load(Config config);

    String getSection();
    String getName();
}
