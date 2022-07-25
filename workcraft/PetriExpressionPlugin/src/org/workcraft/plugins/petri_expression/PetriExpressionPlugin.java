package org.workcraft.plugins.petri_expression;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.petri_expression.commands.InsertPetriCommand;

@SuppressWarnings("unused")
public class PetriExpressionPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Petri Expression plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerCommand(InsertPetriCommand.class);
    }

}
