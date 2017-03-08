package org.workcraft.plugins.plato;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.plato.commands.PlatoConversionCommand;
import org.workcraft.plugins.plato.interop.PlatoImporter;

public class PlatoModule implements Module {

    @Override
    public String getDescription() {
        return "Concept translation support";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(Settings.class, PlatoSettings.class);

        pm.registerClass(Command.class, PlatoConversionCommand.class);
        pm.registerClass(Importer.class, PlatoImporter.class);
    }

}
