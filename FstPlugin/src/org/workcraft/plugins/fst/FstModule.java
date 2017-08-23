package org.workcraft.plugins.fst;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Version;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.fst.commands.ExtractWindowsCommand;
import org.workcraft.plugins.fst.commands.FsmToFstConversionCommand;
import org.workcraft.plugins.fst.commands.FstToFsmConversionCommand;
import org.workcraft.plugins.fst.commands.FstToStgConversionCommand;
import org.workcraft.plugins.fst.commands.PetriToFsmConversionCommand;
import org.workcraft.plugins.fst.commands.StgToBinaryFstConversionCommand;
import org.workcraft.plugins.fst.commands.StgToFstConversionCommand;
import org.workcraft.plugins.fst.interop.SgExporter;
import org.workcraft.plugins.fst.interop.SgImporter;
import org.workcraft.plugins.fst.serialisation.SgSerialiser;
import org.workcraft.serialisation.ModelSerialiser;

public class FstModule implements Module {

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
        pm.registerClass(ModelSerialiser.class, SgSerialiser.class);
        pm.registerClass(Settings.class, ProcessWindowsSettings.class);

        pm.registerClass(Exporter.class, SgExporter.class);
        pm.registerClass(Importer.class, SgImporter.class);

        pm.registerClass(Command.class, StgToFstConversionCommand.class);
        pm.registerClass(Command.class, StgToBinaryFstConversionCommand.class);
        pm.registerClass(Command.class, FstToStgConversionCommand.class);
        pm.registerClass(Command.class, PetriToFsmConversionCommand.class);
        pm.registerClass(Command.class, FsmToFstConversionCommand.class);
        pm.registerClass(Command.class, FstToFsmConversionCommand.class);
        pm.registerClass(Command.class, ExtractWindowsCommand.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.fst.FstModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.fst.FstDescriptor\"/>");
    }

}
