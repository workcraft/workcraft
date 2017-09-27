package org.workcraft.plugins.plato;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.plato.commands.PlatoFstConversionCommand;
import org.workcraft.plugins.plato.commands.PlatoStgConversionCommand;
import org.workcraft.plugins.plato.interop.ConceptsImporter;

public class PlatoModule implements Module {

    @Override
    public String getDescription() {
        return "Plato";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerClass(Settings.class, PlatoSettings.class);
        pm.registerClass(Importer.class, ConceptsImporter.class);

        pm.registerClass(Command.class, PlatoStgConversionCommand.class);
        pm.registerClass(Command.class, PlatoFstConversionCommand.class);
    }

}
