package org.workcraft.plugins.stg;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Version;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.stg.commands.*;
import org.workcraft.plugins.stg.interop.LpnExporter;
import org.workcraft.plugins.stg.interop.LpnImporter;
import org.workcraft.plugins.stg.interop.StgExporter;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.plugins.stg.serialisation.AstgSerialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcDeserialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class StgModule implements Module {

    @Override
    public String getDescription() {
        return "Signal Transition Graphs";
    }

    @Override
    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerClass(ModelDescriptor.class, StgDescriptor.class);
        pm.registerClass(Settings.class, StgSettings.class);

        pm.registerClass(XMLSerialiser.class, ImplicitPlaceArcSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, ImplicitPlaceArcDeserialiser.class);
        pm.registerClass(ModelSerialiser.class, AstgSerialiser.class);

        pm.registerClass(Exporter.class, StgExporter.class);
        pm.registerClass(Importer.class, StgImporter.class);

        pm.registerClass(Exporter.class, LpnExporter.class);
        pm.registerClass(Importer.class, LpnImporter.class);

        ScriptableCommandUtils.register(MirrorSignalTransformationCommand.class, "transformStgMirrorSignal",
                "transform the given STG 'work' by mirroring selected (or all) signals");
        ScriptableCommandUtils.register(MirrorTransitionTransformationCommand.class, "transformStgMirrorTransition",
                "transform the given STG 'work' by mirroring selected (or all) transition sign");
        ScriptableCommandUtils.register(ImplicitPlaceTransformationCommand.class, "transformStgImplicitPlace",
                "transform the given STG 'work' by making selected (or all) places implicit");
        ScriptableCommandUtils.register(ExplicitPlaceTransformationCommand.class, "transformStgExplicitPlace",
                "transform the given STG 'work' by making selected (or all) places explicit");
        ScriptableCommandUtils.register(SignalToDummyTransitionTransformationCommand.class, "transformStgSignalToDummyTransition",
                "transform the given STG 'work' by converting selected signal transitions to dummies");
        ScriptableCommandUtils.register(DummyToSignalTransitionTransformationCommand.class, "transformStgDummyToSignalTransition",
                "transform the given STG 'work' by converting selected dummies to signal transitions");
        ScriptableCommandUtils.register(ContractNamedTransitionTransformationCommand.class, "transformStgContractNamedTransition",
                "transform the given STG 'work' by contracting a selected transition");
        ScriptableCommandUtils.register(MergeTransitionTransformationCommand.class, "transformStgMergeTransition",
                "transform the given STG 'work' by merging selected transitions");
        ScriptableCommandUtils.register(InsertDummyTransformationCommand.class, "transformStgInsertDummy",
                "transform the given STG 'work' by inserting dummies into selected arcs");
        ScriptableCommandUtils.register(ExpandHandshakeTransformationCommand.class, "transformStgExpandHandshake",
                "transform the given STG 'work' by expanding selected handshake transitions");
        ScriptableCommandUtils.register(ExpandHandshakeReqAckTransformationCommand.class, "transformStgExpandHandshakeReqAck",
                "transform the given STG 'work' by expanding selected handshake transitions by adding _req and _ack suffixes");
        ScriptableCommandUtils.register(SelectAllSignalTransitionsTransformationCommand.class, "transformStgSelectAllSignalTransitions",
                "select all transitions of selected signals in the given STG 'work'");

        ScriptableCommandUtils.register(PetriToStgConversionCommand.class, "convertPetriToStg",
                "convert the given Petri net 'work' into a new STG work");
        ScriptableCommandUtils.register(StgToPetriConversionCommand.class, "convertStgToPetri",
                "convert the given STG 'work' into a new Petri net work");

        ScriptableCommandUtils.register(StgStatisticsCommand.class, "statStg",
                "advanced complexity estimates for the STG 'work'");
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();
        Version v320 = new Version(3, 2, 0, Version.Status.RELEASE);
        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);
        Version v314 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.stg.STGModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.stg.StgDescriptor\"/>");

        cm.registerModelReplacement(v310, "org.workcraft.plugins.stg.STG", Stg.class.getName());

        cm.registerModelReplacement(v310, "org.workcraft.plugins.stg.VisualSTG", VisualStg.class.getName());

        cm.registerGlobalReplacement(v310, Stg.class.getName(), "<STGPlace>", "<StgPlace>");

        cm.registerGlobalReplacement(v310, Stg.class.getName(), "</STGPlace>", "</StgPlace>");

        cm.registerContextualReplacement(v320, Stg.class.getName(), "SignalTransition",
                "<property class=\"org.workcraft.plugins.stg.SignalTransition\\$Type\" enum-class=\"org.workcraft.plugins.stg.SignalTransition\\$Type\" name=\"signalType\" value=",
                "<property class=\"org.workcraft.plugins.stg.Signal\\$Type\" enum-class=\"org.workcraft.plugins.stg.Signal\\$Type\" name=\"signalType\" value=");


        cm.registerGlobalReplacement(v310, Stg.class.getName(),
                "<node class=\"org.workcraft.plugins.stg.STGPlace\" ref=",
                "<node class=\"org.workcraft.plugins.stg.StgPlace\" ref=");

        cm.registerGlobalReplacement(v314, VisualStg.class.getName(),
                "<node class=\"org.workcraft.plugins.petri.VisualPlace\" ref=",
                "<node class=\"org.workcraft.plugins.stg.VisualStgPlace\" ref=");

        cm.registerGlobalReplacement(v314, VisualStg.class.getName(),
                "<VisualPlace ref=\"(.*?)\"/>",
                "<VisualStgPlace ref=\"$1\"/>");

        cm.registerGlobalReplacement(v314, VisualStg.class.getName(),
                "<VisualPlace ref=\"(.*?)\">",
                "<VisualStgPlace ref=\"$1\"/>\\n<VisualPlace>");
    }

}
