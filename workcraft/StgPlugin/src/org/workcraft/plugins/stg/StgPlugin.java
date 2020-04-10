package org.workcraft.plugins.stg;

import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.Version;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.plugins.stg.commands.*;
import org.workcraft.plugins.stg.interop.LpnExporter;
import org.workcraft.plugins.stg.interop.LpnImporter;
import org.workcraft.plugins.stg.interop.StgExporter;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcDeserialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;

@SuppressWarnings("unused")
public class StgPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Signal Transition Graphs plugin";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerModelDescriptor(StgDescriptor.class);
        pm.registerSettings(StgSettings.class);

        pm.registerXmlSerialiser(ImplicitPlaceArcSerialiser.class);
        pm.registerXmlDeserialiser(ImplicitPlaceArcDeserialiser.class);

        pm.registerExporter(StgExporter.class);
        pm.registerImporter(StgImporter.class);

        pm.registerExporter(LpnExporter.class);
        pm.registerImporter(LpnImporter.class);

        ScriptableCommandUtils.registerCommand(MirrorSignalTransformationCommand.class, "transformStgMirrorSignal",
                "transform the given STG 'work' by mirroring selected (or all) signals");
        ScriptableCommandUtils.registerCommand(MirrorTransitionTransformationCommand.class, "transformStgMirrorTransition",
                "transform the given STG 'work' by mirroring selected (or all) transition sign");
        ScriptableCommandUtils.registerCommand(ImplicitPlaceTransformationCommand.class, "transformStgImplicitPlace",
                "transform the given STG 'work' by making selected (or all) places implicit");
        ScriptableCommandUtils.registerCommand(ExplicitPlaceTransformationCommand.class, "transformStgExplicitPlace",
                "transform the given STG 'work' by making selected (or all) places explicit");
        ScriptableCommandUtils.registerCommand(SignalToDummyTransitionTransformationCommand.class, "transformStgSignalToDummyTransition",
                "transform the given STG 'work' by converting selected signal transitions to dummies");
        ScriptableCommandUtils.registerCommand(DummyToSignalTransitionTransformationCommand.class, "transformStgDummyToSignalTransition",
                "transform the given STG 'work' by converting selected dummies to signal transitions");
        ScriptableCommandUtils.registerCommand(ContractNamedTransitionTransformationCommand.class, "transformStgContractNamedTransition",
                "transform the given STG 'work' by contracting a selected transition");
        ScriptableCommandUtils.registerCommand(MergeTransitionTransformationCommand.class, "transformStgMergeTransition",
                "transform the given STG 'work' by merging selected transitions");
        ScriptableCommandUtils.registerCommand(InsertDummyTransformationCommand.class, "transformStgInsertDummy",
                "transform the given STG 'work' by inserting dummies into selected arcs");
        ScriptableCommandUtils.registerCommand(ExpandHandshakeTransformationCommand.class, "transformStgExpandHandshake",
                "transform the given STG 'work' by expanding selected handshake transitions");
        ScriptableCommandUtils.registerCommand(ExpandHandshakeReqAckTransformationCommand.class, "transformStgExpandHandshakeReqAck",
                "transform the given STG 'work' by expanding selected handshake transitions by adding _req and _ack suffixes");
        ScriptableCommandUtils.registerCommand(SelectAllSignalTransitionsTransformationCommand.class, "transformStgSelectAllSignalTransitions",
                "select all transitions of selected signals in the given STG 'work'");

        ScriptableCommandUtils.registerCommand(PetriToStgConversionCommand.class, "convertPetriToStg",
                "convert the given Petri net 'work' into a new STG work");
        ScriptableCommandUtils.registerCommand(StgToPetriConversionCommand.class, "convertStgToPetri",
                "convert the given STG 'work' into a new Petri net work");

        ScriptableCommandUtils.registerCommand(StgStatisticsCommand.class, "statStg",
                "advanced complexity estimates for the STG 'work'");
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.stg.STGModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.stg.StgDescriptor\"/>");

        cm.registerModelReplacement(v310, "org.workcraft.plugins.stg.STG", Stg.class.getName());

        cm.registerModelReplacement(v310, "org.workcraft.plugins.stg.VisualSTG", VisualStg.class.getName());

        cm.registerGlobalReplacement(v310, Stg.class.getName(), "<STGPlace>", "<StgPlace>");

        cm.registerGlobalReplacement(v310, Stg.class.getName(), "</STGPlace>", "</StgPlace>");

        cm.registerGlobalReplacement(v310, Stg.class.getName(),
                "<node class=\"org.workcraft.plugins.stg.STGPlace\" ref=",
                "<node class=\"org.workcraft.plugins.stg.StgPlace\" ref=");

        Version v314 = new Version(3, 1, 4, Version.Status.RELEASE);

        cm.registerGlobalReplacement(v314, VisualStg.class.getName(),
                "<node class=\"org.workcraft.plugins.petri.VisualPlace\" ref=",
                "<node class=\"org.workcraft.plugins.stg.VisualStgPlace\" ref=");

        cm.registerGlobalReplacement(v314, VisualStg.class.getName(),
                "<VisualPlace ref=\"(.*?)\"/>",
                "<VisualStgPlace ref=\"$1\"/>");

        cm.registerGlobalReplacement(v314, VisualStg.class.getName(),
                "<VisualPlace ref=\"(.*?)\">",
                "<VisualStgPlace ref=\"$1\"/>\\n<VisualPlace>");

        Version v320 = new Version(3, 2, 0, Version.Status.RELEASE);

        cm.registerContextualReplacement(v320, Stg.class.getName(), "SignalTransition",
                "<property class=\"org.workcraft.plugins.stg.SignalTransition\\$Type\" enum-class=\"org.workcraft.plugins.stg.SignalTransition\\$Type\" name=\"signalType\" value=",
                "<property class=\"org.workcraft.plugins.stg.Signal\\$Type\" enum-class=\"org.workcraft.plugins.stg.Signal\\$Type\" name=\"signalType\" value=");
    }

}
