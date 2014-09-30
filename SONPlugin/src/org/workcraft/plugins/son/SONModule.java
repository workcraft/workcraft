package org.workcraft.plugins.son;

import org.workcraft.Framework;
import org.workcraft.Module;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.son.tools.ColorResetTool;
import org.workcraft.plugins.son.tools.ErrTracingDisable;
import org.workcraft.plugins.son.tools.ErrTracingReset;
import org.workcraft.plugins.son.tools.StructurePropertyChecker;
import org.workcraft.plugins.son.tools.TestTool;
import org.workcraft.plugins.son.tools.TokenRefreshTool;
//import org.workcraft.plugins.son.tools.TestTool;


public class SONModule implements Module{

	public void init(Framework framework) {
		framework.getPluginManager().registerClass(ModelDescriptor.class, SONModelDescriptor.class);
		framework.getPluginManager().registerClass(Settings.class, SONSettings.class);
		framework.getPluginManager().registerClass(Tool.class, TestTool.class, framework);
		//structural verification
		framework.getPluginManager().registerClass(Tool.class, StructurePropertyChecker.class, framework);
		//Custom tools
		framework.getPluginManager().registerClass(Tool.class, ColorResetTool.class);
		framework.getPluginManager().registerClass(Tool.class, TokenRefreshTool.class);
		//error tracing
		framework.getPluginManager().registerClass(Tool.class, ErrTracingReset.class);
		framework.getPluginManager().registerClass(Tool.class, ErrTracingDisable.class);

	}

	public String getDescription() {
		return "Structured Occurrence Nets";
	}
}
