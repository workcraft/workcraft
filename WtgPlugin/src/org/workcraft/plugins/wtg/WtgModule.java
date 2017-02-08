package org.workcraft.plugins.wtg;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.wtg.interop.DotGExporter;
import org.workcraft.plugins.wtg.serialisation.DotGSerialiser;
import org.workcraft.serialisation.ModelSerialiser;

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
        pm.registerClass(Exporter.class, DotGExporter.class);
        pm.registerClass(ModelSerialiser.class, DotGSerialiser.class);
    }

}
