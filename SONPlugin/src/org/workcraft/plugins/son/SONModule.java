package org.workcraft.plugins.son;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.son.tools.ColorResetTool;
import org.workcraft.plugins.son.tools.ErrTracingDisable;
import org.workcraft.plugins.son.tools.ErrTracingReset;
import org.workcraft.plugins.son.tools.ReachabilityTool;
import org.workcraft.plugins.son.tools.StructurePropertyChecker;
import org.workcraft.plugins.son.tools.TestTool;
import org.workcraft.plugins.son.tools.TokenRefreshTool;

public class SONModule implements Module{

	public String getDescription() {
		return "Structured Occurrence Nets";
	}

	public void init(Framework framework) {
		initPluginManager(framework);
		initCompatibilityManager(framework);
	}

	private void initPluginManager(final Framework framework) {
		PluginManager pm = framework.getPluginManager();
		pm.registerClass(ModelDescriptor.class, SONModelDescriptor.class);
		pm.registerClass(Settings.class, SONSettings.class);
		pm.registerClass(Tool.class, TestTool.class, framework);
		//verification
		pm.registerClass(Tool.class, StructurePropertyChecker.class, framework);
		pm.registerClass(Tool.class, ReachabilityTool.class);
		//Custom tools
		pm.registerClass(Tool.class, ColorResetTool.class);
		pm.registerClass(Tool.class, TokenRefreshTool.class);
		// Error tracing
		pm.registerClass(Tool.class, ErrTracingReset.class);
		pm.registerClass(Tool.class, ErrTracingDisable.class);

	}

	private void initCompatibilityManager(final Framework framework) {
		final CompatibilityManager cm = framework.getCompatibilityManager();

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
				"<graphic class=\"org.workcraft.plugins.son.connections.BhvLine\" ref=\"(.*?)()\"/>",
				"<graphic class=\"org.workcraft.dom.visual.connections.Polyline\" ref=\"$1\"/>");
	}

}
