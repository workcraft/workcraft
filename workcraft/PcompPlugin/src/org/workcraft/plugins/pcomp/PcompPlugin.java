package org.workcraft.plugins.pcomp;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.pcomp.commands.ParallelCompositionCommand;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class PcompPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "PComp parallel composition plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerSettings(PcompSettings.class);

        ScriptableCommandUtils.registerDataCommand(ParallelCompositionCommand.class, "composeStg",
                "compose STGs specified by space-separated list of work file names 'data' ('work' parameter is ignored)");
    }

}
