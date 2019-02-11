package org.workcraft.plugins.plato;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.plugins.plato.commands.PlatoFstConversionCommand;
import org.workcraft.plugins.plato.commands.PlatoStgConversionCommand;
import org.workcraft.plugins.plato.interop.ConceptsImporter;

public class PlatoModule implements org.workcraft.Module {

    @Override
    public String getDescription() {
        return "Plato";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerSettings(PlatoSettings.class);
        pm.registerImporter(ConceptsImporter.class);

        pm.registerCommand(PlatoStgConversionCommand.class);
        pm.registerCommand(PlatoFstConversionCommand.class);
    }

}
