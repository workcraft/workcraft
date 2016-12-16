package org.workcraft.plugins.cpog;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.PropertyClassProvider;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.cpog.scenco.ExhaustiveSearchScencoCommand;
import org.workcraft.plugins.cpog.scenco.HeuristicSearchScencoCommand;
import org.workcraft.plugins.cpog.scenco.RandomSearchScencoCommand;
import org.workcraft.plugins.cpog.scenco.SatBasedScencoCommand;
import org.workcraft.plugins.cpog.scenco.SingleLiteralScencoCommand;
import org.workcraft.plugins.cpog.scenco.SequentialScencoCommand;
import org.workcraft.plugins.cpog.serialisation.ArcDeserialiser;
import org.workcraft.plugins.cpog.serialisation.ArcSerialiser;
import org.workcraft.plugins.cpog.serialisation.RhoClauseDeserialiser;
import org.workcraft.plugins.cpog.serialisation.RhoClauseSerialiser;
import org.workcraft.plugins.cpog.serialisation.VertexDeserialiser;
import org.workcraft.plugins.cpog.serialisation.VertexSerialiser;
import org.workcraft.plugins.cpog.serialisation.VisualCPOGGroupDeserialiser;
import org.workcraft.plugins.cpog.serialisation.VisualCPOGGroupSerialiser;
import org.workcraft.plugins.cpog.tools.AlgebraExpressionFromGraphsCommand;
import org.workcraft.plugins.cpog.tools.AlgebraImportCommand;
import org.workcraft.plugins.cpog.tools.CpogToGraphConversionCommand;
import org.workcraft.plugins.cpog.tools.GraphStatisticsCommand;
import org.workcraft.plugins.cpog.tools.GraphToCpogConversionCommand;
import org.workcraft.plugins.cpog.tools.ImportEventLogPGMinerCommand;
import org.workcraft.plugins.cpog.tools.ExtractSelectedGraphsPGMinerCommand;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class CpogModule implements Module {

    @Override
    public String getDescription() {
        return "Conditional Partial Order Graphs";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, CpogDescriptor.class);

        pm.registerClass(PropertyClassProvider.class, EncodingPropertyProvider.class);

        pm.registerClass(XMLSerialiser.class, VisualCPOGGroupSerialiser.class);
        pm.registerClass(XMLSerialiser.class, VertexSerialiser.class);
        pm.registerClass(XMLSerialiser.class, RhoClauseSerialiser.class);
        pm.registerClass(XMLSerialiser.class, ArcSerialiser.class);

        pm.registerClass(XMLDeserialiser.class, VisualCPOGGroupDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, VertexDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, RhoClauseDeserialiser.class);
        pm.registerClass(XMLDeserialiser.class, ArcDeserialiser.class);
        pm.registerClass(Settings.class, CpogSettings.class);

        pm.registerClass(Command.class, HeuristicSearchScencoCommand.class);
        pm.registerClass(Command.class, SatBasedScencoCommand.class);
        pm.registerClass(Command.class, SingleLiteralScencoCommand.class);
        pm.registerClass(Command.class, SequentialScencoCommand.class);
        pm.registerClass(Command.class, ExhaustiveSearchScencoCommand.class);
        pm.registerClass(Command.class, RandomSearchScencoCommand.class);
        pm.registerClass(Command.class, GraphStatisticsCommand.class);
        pm.registerClass(Command.class, CpogToGraphConversionCommand.class);
        pm.registerClass(Command.class, GraphToCpogConversionCommand.class);
        pm.registerClass(Command.class, ImportEventLogPGMinerCommand.class);
        pm.registerClass(Command.class, ExtractSelectedGraphsPGMinerCommand.class);
        pm.registerClass(Command.class, AlgebraImportCommand.class);
        pm.registerClass(Command.class, AlgebraExpressionFromGraphsCommand.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.cpog.CpogModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.cpog.CpogDescriptor\"/>");

        cm.registerModelReplacement("org.workcraft.plugins.cpog.CPOG", Cpog.class.getName());

        cm.registerModelReplacement("org.workcraft.plugins.cpog.VisualCPOG", VisualCpog.class.getName());
    }

}
