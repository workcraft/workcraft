package org.workcraft.plugins.fst;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.fst.interop.DotGExporter;
import org.workcraft.plugins.fst.interop.DotGImporter;
import org.workcraft.plugins.fst.serialisation.DotGSerialiser;
import org.workcraft.plugins.fst.tools.FsmToFstConversionCommand;
import org.workcraft.plugins.fst.tools.FstToFsmConversionCommand;
import org.workcraft.plugins.fst.tools.FstToStgConversionCommand;
import org.workcraft.plugins.fst.tools.PetriToFsmConversionCommand;
import org.workcraft.plugins.fst.tools.StgToFstConversionCommand;
import org.workcraft.serialisation.ModelSerialiser;

public class FstModule implements Module {

    private final class StgToBinaryFstConversionCommand extends StgToFstConversionCommand {
        @Override
        public boolean isBinary() {
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "Finite State Transducer";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, FstDescriptor.class);

        pm.registerClass(Exporter.class, DotGExporter.class);
        pm.registerClass(Importer.class, DotGImporter.class);

        pm.registerClass(ModelSerialiser.class, DotGSerialiser.class);

        pm.registerClass(Command.class, StgToFstConversionCommand.class);
        pm.registerClass(Command.class, FstToStgConversionCommand.class);
        pm.registerClass(Command.class, PetriToFsmConversionCommand.class);
        pm.registerClass(Command.class, FsmToFstConversionCommand.class);
        pm.registerClass(Command.class, FstToFsmConversionCommand.class);

        pm.registerClass(Command.class, new Initialiser<Command>() {
            @Override
            public Command create() {
                return new StgToBinaryFstConversionCommand();
            }
        });
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.fst.FstModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.fst.FstDescriptor\"/>");
    }

}
