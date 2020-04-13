package org.workcraft.plugins.petri;

import org.workcraft.Framework;
import org.workcraft.Version;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.petri.commands.*;
import org.workcraft.plugins.petri.serialization.ReadArcDeserialiser;
import org.workcraft.plugins.petri.serialization.ReadArcSerialiser;
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class PetriPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Petri Net plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerModelDescriptor(PetriDescriptor.class);

        pm.registerXmlSerialiser(ReadArcSerialiser.class);
        pm.registerXmlDeserialiser(ReadArcDeserialiser.class);

        ScriptableCommandUtils.registerCommand(ContractTransitionTransformationCommand.class, "transformPetriContractTransition",
                "transform the Petri net 'work' by contracting a selected transition");
        ScriptableCommandUtils.registerCommand(MergeTransitionTransformationCommand.class, "transformPetriMergeTransition",
                "transform the Petri net 'work' by merging selected transitions");
        ScriptableCommandUtils.registerCommand(DirectedArcToReadArcTransformationCommand.class, "transformPetriDirectedArcToReadArc",
                "transform the Petri net (or derived model, e.g.STG) 'work' by converting selected arcs to read-arcs");
        ScriptableCommandUtils.registerCommand(DualArcToReadArcTransformationCommand.class, "transformPetriDualArcToReadArc",
                "transform the Petri net (or derived model, e.g.STG) 'work' by converting selected (or all) dual producing/consuming arcs to read-arcs");
        ScriptableCommandUtils.registerCommand(ReadArcToDualArcTransformationCommand.class, "transformPetriReadArcToDualArc",
                "transform the Petri net (or derived model, e.g.STG) 'work' by converting selected (or all) read-arcs to dual producing/consuming arcs");
        ScriptableCommandUtils.registerCommand(CollapseProxyTransformationCommand.class, "transformPetriCollapseProxy",
                "transform the Petri net (or derived model, e.g.STG) 'work' by collapsing selected (or all) proxy places");
        ScriptableCommandUtils.registerCommand(ProxyDirectedArcPlaceTransformationCommand.class, "transformPetriProxyDirectedArcPlace",
                "transform the Petri net (or derived model, e.g.STG) 'work' by creating proxies for selected producing/consuming arc places");
        ScriptableCommandUtils.registerCommand(ProxyReadArcPlaceTransformationCommand.class, "transformPetriProxyReadArcPlace",
                "transform the Petri net (or derived model, e.g.STG) 'work' by creating selected (or all) proxies for read-arc places");
        ScriptableCommandUtils.registerCommand(MergePlaceTransformationCommand.class, "transformPetriMergePlace",
                "transform the Petri net (or derived model, e.g.STG) 'work' by merging selected places");

        ScriptableCommandUtils.registerCommand(PetriStatisticsCommand.class, "statPetri",
                "advanced complexity estimates for the Petri net 'work'");
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetDescriptor\"/>");

        Version v323 = new Version(3, 2, 3, Version.Status.RELEASE);

        cm.registerMetaReplacement(v323,
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.petri.PetriDescriptor\"/>");

        cm.registerModelReplacement(v323, "org.workcraft.plugins.petri.PetriNet", Petri.class.getName());

        cm.registerModelReplacement(v323, "org.workcraft.plugins.petri.VisualPetriNet", VisualPetri.class.getName());
    }

}
