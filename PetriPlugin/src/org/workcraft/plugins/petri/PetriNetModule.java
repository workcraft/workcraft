package org.workcraft.plugins.petri;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.petri.commands.CollapseProxyTransformationCommand;
import org.workcraft.plugins.petri.commands.ContractTransitionTransformationCommand;
import org.workcraft.plugins.petri.commands.DirectedArcToReadArcTransformationCommand;
import org.workcraft.plugins.petri.commands.DualArcToReadArcTransformationCommand;
import org.workcraft.plugins.petri.commands.MergePlaceTransformationCommand;
import org.workcraft.plugins.petri.commands.MergeTransitionTransformationCommand;
import org.workcraft.plugins.petri.commands.ProxyDirectedArcPlaceTransformationCommand;
import org.workcraft.plugins.petri.commands.ProxyReadArcPlaceTransformationCommand;
import org.workcraft.plugins.petri.commands.ReadArcToDualArcTransformationCommand;
import org.workcraft.plugins.petri.serialization.ReadArcDeserialiser;
import org.workcraft.plugins.petri.serialization.ReadArcSerialiser;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class PetriNetModule implements Module {

    @Override
    public String getDescription() {
        return "Petri Net";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerClass(ModelDescriptor.class, PetriNetDescriptor.class);

        pm.registerClass(XMLSerialiser.class, ReadArcSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, ReadArcDeserialiser.class);

        pm.registerClass(Command.class, ContractTransitionTransformationCommand.class);
        pm.registerClass(Command.class, DirectedArcToReadArcTransformationCommand.class);
        pm.registerClass(Command.class, DualArcToReadArcTransformationCommand.class);
        pm.registerClass(Command.class, ReadArcToDualArcTransformationCommand.class);
        pm.registerClass(Command.class, CollapseProxyTransformationCommand.class);
        pm.registerClass(Command.class, ProxyDirectedArcPlaceTransformationCommand.class);
        pm.registerClass(Command.class, ProxyReadArcPlaceTransformationCommand.class);
        pm.registerClass(Command.class, MergePlaceTransformationCommand.class);
        pm.registerClass(Command.class, MergeTransitionTransformationCommand.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetDescriptor\"/>");
    }

}
