package org.workcraft.plugins.dtd;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;

@SuppressWarnings("unused")
public class DtdPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Digital Timing Diagram plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(DtdDescriptor.class);

        pm.registerSettings(DtdSettings.class);
    }

}
