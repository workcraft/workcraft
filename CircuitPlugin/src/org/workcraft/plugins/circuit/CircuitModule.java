package org.workcraft.plugins.circuit;

import org.workcraft.CompatibilityManager;
import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.circuit.serialisation.FunctionDeserialiser;
import org.workcraft.plugins.circuit.serialisation.FunctionSerialiser;
import org.workcraft.plugins.circuit.tools.CheckCircuitTool;
import org.workcraft.plugins.circuit.tools.StgGeneratorTool;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;


public class CircuitModule implements Module {

	@Override
	public String getDescription() {
		return "Gate-level circuit model";
	}

	@Override
	public void init() {
		initPluginManager();
		initCompatibilityManager();
	}

	private void initPluginManager() {
		final Framework framework = Framework.getInstance();
		PluginManager pm = framework.getPluginManager();

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new StgGeneratorTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckCircuitTool();
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckCircuitTool() {
					@Override
					public String getDisplayName() {
						return " Conformation [MPSat]";
					}
					@Override
					public boolean checkDeadlock() {
						return false;
					}
					@Override
					public boolean checkHazard() {
						return false;
					}
				};
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckCircuitTool() {
					@Override
					public String getDisplayName() {
						return " Deadlock [MPSat]";
					}
					@Override
					public boolean checkConformation() {
						return false;
					}
					@Override
					public boolean checkHazard() {
						return false;
					}
				};
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new CheckCircuitTool() {
					@Override
					public String getDisplayName() {
						return " Hazard [MPSat]";
					}
					@Override
					public boolean checkConformation() {
						return false;
					}
					@Override
					public boolean checkDeadlock() {
						return false;
					}
				};
			}
		});

		pm.registerClass(ModelDescriptor.class, CircuitModelDescriptor.class);
		pm.registerClass(XMLSerialiser.class, FunctionSerialiser.class);
		pm.registerClass(XMLDeserialiser.class, FunctionDeserialiser.class);
		pm.registerClass(Settings.class, CircuitSettings.class);
	}


	private void initCompatibilityManager() {
		final Framework framework = Framework.getInstance();
		final CompatibilityManager cm = framework.getCompatibilityManager();

		cm.registerContextualReplacement(VisualCircuit.class.getName(), "VisualCircuitComponent",
				"<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"C_ELEMENT\"/>",
				"<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"GATE\"/>");

		cm.registerContextualReplacement(VisualCircuit.class.getName(), "VisualCircuitComponent",
				"<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"BUFFER\"/>",
				"<property class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" enum-class=\"org.workcraft.plugins.circuit.renderers.ComponentRenderingResult\\$RenderType\" name=\"renderType\" value=\"GATE\"/>");

		cm.registerContextualReplacement(Circuit.class.getName(), "Contact",
				"<property class=\"boolean\" name=\"initOne\" value=\"(.*?)\"/>",
				"<property class=\"boolean\" name=\"initToOne\" value=\"$1\"/>");
	}

}
