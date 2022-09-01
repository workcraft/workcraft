package org.workcraft.plugins.cflt;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.cflt.commands.TranslateProFloExpressionCommand;

@SuppressWarnings("unused")
public class CfltPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Cflt plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerCommand(TranslateProFloExpressionCommand.class);
    }

}
