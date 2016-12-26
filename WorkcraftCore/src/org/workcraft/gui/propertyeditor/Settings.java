package org.workcraft.gui.propertyeditor;

import org.workcraft.Config;

public interface Settings extends Properties {
    void save(Config config);
    void load(Config config);

    String getSection();
    String getName();
}
