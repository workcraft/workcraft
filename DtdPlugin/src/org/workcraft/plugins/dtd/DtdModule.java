package org.workcraft.plugins.dtd;

import org.workcraft.Framework;
import org.workcraft.PluginManager;

public class DtdModule implements org.workcraft.Module {

    @Override
    public String getDescription() {
        return "Digital Timing Diagram";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(DtdDescriptor.class);

        pm.registerSettings(DtdSettings.class);
    }

}
