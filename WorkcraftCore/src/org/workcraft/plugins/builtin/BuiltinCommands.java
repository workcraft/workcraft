package org.workcraft.plugins.builtin;

import org.workcraft.Framework;
import org.workcraft.plugins.builtin.commands.AnonymiseTransformationCommand;
import org.workcraft.plugins.builtin.commands.BasicStatisticsCommand;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.builtin.commands.StraightenConnectionTransformationCommand;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.commands.DotLayoutCommand;
import org.workcraft.plugins.builtin.commands.RandomLayoutCommand;

@SuppressWarnings("unused")
public class BuiltinCommands implements Plugin {

    @Override
    public String getDescription() {
        return "Built-in commands";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        ScriptableCommandUtils.register(AnonymiseTransformationCommand.class, "transformModelAnonymise",
                "anonymise the given 'work' by randomly renaming its nodes");

        ScriptableCommandUtils.register(StraightenConnectionTransformationCommand.class, "transformModelStraightenConnection",
                "transform the given 'work' by straightening selected (or all) arcs");

        ScriptableCommandUtils.register(DotLayoutCommand.class, "layoutModelDot",
                "position nodes and shape the arcs using of the model ''work'' using //Graphviz// backend");

        ScriptableCommandUtils.register(RandomLayoutCommand.class, "layoutModelRandom",
                "randomly position graph nodes of the model ''work'' and connect them by straight arcs");

        ScriptableCommandUtils.register(BasicStatisticsCommand.class, "statModel",
                "node and arc count for the model 'work' (all model types are supported)");
    }

}
