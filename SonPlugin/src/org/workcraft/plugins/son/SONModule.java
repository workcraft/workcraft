package org.workcraft.plugins.son;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Command;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.son.tools.ColorResetCommand;
import org.workcraft.plugins.son.tools.ErrTracingDisableCommand;
import org.workcraft.plugins.son.tools.ErrTracingResetCommand;
import org.workcraft.plugins.son.tools.ReachabilityTool;
import org.workcraft.plugins.son.tools.StructurePropertyChecker;
import org.workcraft.plugins.son.tools.TestTool;
import org.workcraft.plugins.son.tools.TimeConsistencyChecker;
import org.workcraft.plugins.son.tools.TimeValueDisable;
import org.workcraft.plugins.son.tools.ClearMarkingCommand;
import org.workcraft.plugins.son.tools.TimeValueSetter;

public class SONModule implements Module {

    public String getDescription() {
        return "Structured Occurrence Nets";
    }

    public void init() {
        initPluginManager();
        initCompatibilityManager();
    }

    private void initPluginManager() {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();

        pm.registerClass(ModelDescriptor.class, SONDescriptor.class);
        pm.registerClass(Settings.class, SONSettings.class);
        pm.registerClass(Command.class, TestTool.class);
        //verification
        pm.registerClass(Command.class, StructurePropertyChecker.class);
        pm.registerClass(Command.class, ReachabilityTool.class);
        //Custom tools
        pm.registerClass(Command.class, ColorResetCommand.class);
        pm.registerClass(Command.class, ClearMarkingCommand.class);
        // Error tracing
        pm.registerClass(Command.class, ErrTracingResetCommand.class);
        pm.registerClass(Command.class, ErrTracingDisableCommand.class);
        //time analysis
        pm.registerClass(Command.class, TimeValueSetter.class);
        pm.registerClass(Command.class, TimeValueDisable.class);
        //pm.registerClass(Tool.class, TimeValueEstimator.class);
        pm.registerClass(Command.class, TimeConsistencyChecker.class);
    }

    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        cm.registerMetaReplacement(
                "<descriptor class=\"org.workcraft.plugins.son.SONModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.son.SONDescriptor\"/>");

        cm.registerGlobalReplacement(VisualSON.class.getName(),
                "<VisualONGroup mathGroup=",
                "<VisualONGroup ref=");

        // PNLINE
        cm.registerContextualReplacement(VisualSON.class.getName(), "VisualSONConnection",
                "<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"(.*?)\"/>",
                "<property class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" enum-class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" name=\"semantics\" value=\"PNLINE\"/>");

        // SYNCLINE
        cm.registerContextualReplacement(VisualSON.class.getName(), "VisualSONConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.SyncLine\" ref=\"(.*?)\"/>",
                "<property class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" enum-class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" name=\"semantics\" value=\"SYNCLINE\"/>");

        cm.registerContextualReplacement(VisualSON.class.getName(), "VisualConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.SyncLine\" ref=\"(.*?)\"/>",
                "<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"$1\"/>");

        // ASYNLINE
        cm.registerContextualReplacement(VisualSON.class.getName(), "VisualSONConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.AsynLine\" ref=\"(.*?)\"/>",
                "<property class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" enum-class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" name=\"semantics\" value=\"ASYNLINE\"/>");

        cm.registerContextualReplacement(VisualSON.class.getName(), "VisualConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.AsynLine\" ref=\"(.*?)\"/>",
                "<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"$1\"/>");

        // BHVLINE
        cm.registerContextualReplacement(VisualSON.class.getName(), "VisualSONConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.BhvLine\" ref=\"(.*?)\"/>",
                "<property class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" enum-class=\"org.workcraft.plugins.son.connections.SONConnection\\$Semantics\" name=\"semantics\" value=\"BHVLINE\"/>");

        cm.registerContextualReplacement(VisualSON.class.getName(), "VisualConnection",
                "<graphic class=\"org.workcraft.plugins.son.connections.BhvLine\" ref=\"(.*?)\"/>",
                "<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"$1\"/>");
    }

}
