package org.workcraft.plugins.builtin;

import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.workspace.SystemOpen;
import org.workcraft.plugins.builtin.workspace.WorkcraftOpen;

@SuppressWarnings("unused")
public class BuiltinWorkspace implements Plugin {

    @Override
    public String getDescription() {
        return "Built-in workspace operations";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerFileHandler(() -> new WorkcraftOpen());
        pm.registerFileHandler(SystemOpen.class);
    }

}
