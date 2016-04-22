package org.workcraft.plugins.dtd;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;

public class DtdModule  implements Module {

    @Override
    public String getDescription() {
        return "Digital Timing Diagram";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, DtdDescriptor.class);
    }

}
