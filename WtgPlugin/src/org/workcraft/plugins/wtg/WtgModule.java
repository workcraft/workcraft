package org.workcraft.plugins.wtg;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.wtg.commands.WtgToStgConversionCommand;
import org.workcraft.plugins.wtg.interop.WtgExporter;
import org.workcraft.plugins.wtg.serialisation.WtgSerialiser;
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
        pm.registerClass(Settings.class, WaverSettings.class);
        pm.registerClass(ModelSerialiser.class, WtgSerialiser.class);

        pm.registerClass(Exporter.class, WtgExporter.class);

        pm.registerClass(Command.class, WtgToStgConversionCommand.class);
    }

}
