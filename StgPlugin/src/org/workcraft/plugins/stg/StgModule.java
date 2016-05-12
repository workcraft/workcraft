package org.workcraft.plugins.stg;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.stg.interop.DotGExporter;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.plugins.stg.serialisation.DotGSerialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcDeserialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;
import org.workcraft.plugins.stg.tools.DummyInserterTool;
import org.workcraft.plugins.stg.tools.DummyToSignalTransitionConverterTool;
import org.workcraft.plugins.stg.tools.MakePlacesExplicitTool;
import org.workcraft.plugins.stg.tools.MakePlacesImplicitTool;
import org.workcraft.plugins.stg.tools.NamedTransitionContractorTool;
import org.workcraft.plugins.stg.tools.PetriToStgConverterTool;
import org.workcraft.plugins.stg.tools.SignalMirrorTool;
import org.workcraft.plugins.stg.tools.SignalToDummyTransitionConverterTool;
import org.workcraft.plugins.stg.tools.StgToPetriConverterTool;
import org.workcraft.plugins.stg.tools.TransitionMergerTool;
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

        pm.registerClass(XMLSerialiser.class, ImplicitPlaceArcSerialiser.class);
        pm.registerClass(XMLDeserialiser.class, ImplicitPlaceArcDeserialiser.class);

        pm.registerClass(Exporter.class, DotGExporter.class);
        pm.registerClass(Importer.class, DotGImporter.class);

        pm.registerClass(ModelSerialiser.class, DotGSerialiser.class);
        pm.registerClass(Settings.class, StgSettings.class);

        pm.registerClass(Tool.class, SignalMirrorTool.class);
        pm.registerClass(Tool.class, MakePlacesImplicitTool.class);
        pm.registerClass(Tool.class, MakePlacesExplicitTool.class);
        pm.registerClass(Tool.class, SignalToDummyTransitionConverterTool.class);
        pm.registerClass(Tool.class, DummyToSignalTransitionConverterTool.class);
        pm.registerClass(Tool.class, NamedTransitionContractorTool.class);
        pm.registerClass(Tool.class, PetriToStgConverterTool.class);
        pm.registerClass(Tool.class, StgToPetriConverterTool.class);
        pm.registerClass(Tool.class, TransitionMergerTool.class);
        pm.registerClass(Tool.class, DummyInserterTool.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.stg.STGModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.stg.StgDescriptor\"/>");

        cm.registerModelReplacement("org.workcraft.plugins.stg.STG", Stg.class.getName());

        cm.registerModelReplacement("org.workcraft.plugins.stg.VisualSTG", VisualStg.class.getName());

        cm.registerGlobalReplacement(Stg.class.getName(), "<STGPlace>", "<StgPlace>");

        cm.registerGlobalReplacement(Stg.class.getName(), "</STGPlace>", "</StgPlace>");

        cm.registerGlobalReplacement(Stg.class.getName(),
                "<node class=\"org.workcraft.plugins.stg.STGPlace\" ref=",
                "<node class=\"org.workcraft.plugins.stg.StgPlace\" ref=");
    }

}
