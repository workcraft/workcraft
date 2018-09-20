package org.workcraft.plugins.petri;

import org.workcraft.*;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.plugins.petri.commands.*;
import org.workcraft.plugins.petri.serialization.ReadArcDeserialiser;
import org.workcraft.plugins.petri.serialization.ReadArcSerialiser;

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
        pm.registerModel(PetriNetDescriptor.class);

        pm.registerXmlSerialiser(ReadArcSerialiser.class);
        pm.registerXmlDeserialiser(ReadArcDeserialiser.class);

        ScriptableCommandUtils.register(ContractTransitionTransformationCommand.class, "transformPetriContractTransition",
                "transform the given Petri net 'work' by contracting a selected transition");
        ScriptableCommandUtils.register(MergeTransitionTransformationCommand.class, "transformPetriMergeTransition",
                "transform the given Petri net 'work' by merging selected transitions");
        ScriptableCommandUtils.register(DirectedArcToReadArcTransformationCommand.class, "transformPetriDirectedArcToReadArc",
                "transform the given Petri net (or derived model, e.g.STG) 'work' by converting selected arcs to read-arcs");
        ScriptableCommandUtils.register(DualArcToReadArcTransformationCommand.class, "transformPetriDualArcToReadArc",
                "transform the given Petri net (or derived model, e.g.STG) 'work' by converting selected (or all) dual producing/consuming arcs to read-arcs");
        ScriptableCommandUtils.register(ReadArcToDualArcTransformationCommand.class, "transformPetriReadArcToDualArc",
                "transform the given Petri net (or derived model, e.g.STG) 'work' by converting selected (or all) read-arcs to dual producing/consuming arcs");
        ScriptableCommandUtils.register(CollapseProxyTransformationCommand.class, "transformPetriCollapseProxy",
                "transform the given Petri net (or derived model, e.g.STG) 'work' by collapsing selected (or all) proxy places");
        ScriptableCommandUtils.register(ProxyDirectedArcPlaceTransformationCommand.class, "transformPetriProxyDirectedArcPlace",
                "transform the given Petri net (or derived model, e.g.STG) 'work' by creating proxies for selected producing/consuming arc places");
        ScriptableCommandUtils.register(ProxyReadArcPlaceTransformationCommand.class, "transformPetriProxyReadArcPlace",
                "transform the given Petri net (or derived model, e.g.STG) 'work' by creating selected (or all) proxies for read-arc places");
        ScriptableCommandUtils.register(MergePlaceTransformationCommand.class, "transformPetriMergePlace",
                "transform the given Petri net (or derived model, e.g.STG) 'work' by merging selected places");

        ScriptableCommandUtils.register(PetriStatisticsCommand.class, "statPetri",
                "advanced complexity estimates for the Petri net 'work'");
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.petri.PetriNetDescriptor\"/>");
    }

}
