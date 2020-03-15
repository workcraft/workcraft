package org.workcraft.plugins.cpog;

import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.Version;
import org.workcraft.plugins.cpog.commands.*;
import org.workcraft.plugins.cpog.scenco.*;
import org.workcraft.plugins.cpog.serialisation.*;

@SuppressWarnings("unused")
public class CpogPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Conditional Partial Order Graphs plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerModelDescriptor(CpogDescriptor.class);

        pm.registerProperty(EncodingPropertyProvider.class);

        pm.registerXmlSerialiser(VisualCPOGGroupSerialiser.class);
        pm.registerXmlSerialiser(VertexSerialiser.class);
        pm.registerXmlSerialiser(RhoClauseSerialiser.class);
        pm.registerXmlSerialiser(ArcSerialiser.class);

        pm.registerXmlDeserialiser(VisualCPOGGroupDeserialiser.class);
        pm.registerXmlDeserialiser(VertexDeserialiser.class);
        pm.registerXmlDeserialiser(RhoClauseDeserialiser.class);
        pm.registerXmlDeserialiser(ArcDeserialiser.class);
        pm.registerSettings(CpogSettings.class);

        pm.registerCommand(HeuristicSearchScencoCommand.class);
        pm.registerCommand(SatBasedScencoCommand.class);
        pm.registerCommand(SingleLiteralScencoCommand.class);
        pm.registerCommand(SequentialScencoCommand.class);
        pm.registerCommand(ExhaustiveSearchScencoCommand.class);
        pm.registerCommand(RandomSearchScencoCommand.class);
        pm.registerCommand(GraphStatisticsCommand.class);
        pm.registerCommand(CpogToGraphConversionCommand.class);
        pm.registerCommand(GraphToCpogConversionCommand.class);
        pm.registerCommand(ImportEventLogPGMinerCommand.class);
        pm.registerCommand(ExtractSelectedGraphsPGMinerCommand.class);
        pm.registerCommand(AlgebraImportCommand.class);
        pm.registerCommand(AlgebraExpressionFromGraphsCommand.class);
        pm.registerCommand(PetriToCpogConversionCommand.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.cpog.CpogModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.cpog.CpogDescriptor\"/>");

        cm.registerModelReplacement(v310, "org.workcraft.plugins.cpog.CPOG", Cpog.class.getName());

        cm.registerModelReplacement(v310, "org.workcraft.plugins.cpog.VisualCPOG", VisualCpog.class.getName());
    }

}
