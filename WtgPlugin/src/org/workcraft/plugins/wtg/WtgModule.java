package org.workcraft.plugins.wtg;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;

public class WtgModule  implements Module {

    @Override
    public String getDescription() {
        return "Waveform Transition Graph";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, WtgDescriptor.class);
    }

}
