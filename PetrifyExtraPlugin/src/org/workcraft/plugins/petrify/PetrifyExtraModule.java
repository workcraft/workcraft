package org.workcraft.plugins.petrify;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.commands.ShowSgCommand;

public class PetrifyExtraModule implements Module {

    private final class ShowBinarySgCommand extends ShowSgCommand {
        @Override
        public boolean isBinary() {
            return true;
        }
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerClass(Exporter.class, AstgExporter.class);

        pm.registerClass(Command.class, ShowSgCommand.class);

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new ShowBinarySgCommand();
            }
        });
    }

    @Override
    public String getDescription() {
        return "Petrify state graph support";
    }

}
