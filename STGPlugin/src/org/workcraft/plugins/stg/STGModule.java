package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.Tool;
import org.workcraft.dom.ModelDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.stg.serialisation.DotGSerialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcDeserialiser;
import org.workcraft.plugins.stg.serialisation.ImplicitPlaceArcSerialiser;
import org.workcraft.plugins.stg.tools.MakePlacesExplicitTool;
import org.workcraft.plugins.stg.tools.MakePlacesImplicitTool;
import org.workcraft.plugins.stg.tools.SignalMirrorTool;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.xml.XMLDeserialiser;
import org.workcraft.serialisation.xml.XMLSerialiser;

public class STGModule implements Module {

	@Override
	public void init(final Framework framework) {
		final PluginManager pm = framework.getPluginManager();
		pm.registerClass(ModelDescriptor.class, STGModelDescriptor.class);

		pm.registerClass(XMLSerialiser.class, ImplicitPlaceArcSerialiser.class);
		pm.registerClass(XMLDeserialiser.class, ImplicitPlaceArcDeserialiser.class);

		pm.registerClass(Exporter.class, DotGExporter.class);
		pm.registerClass(Importer.class, DotGImporter.class);

		pm.registerClass(ModelSerialiser.class, DotGSerialiser.class);
		pm.registerClass(SettingsPage.class, STGSettings.class);

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new SignalMirrorTool(framework);
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new MakePlacesImplicitTool(framework);
			}
		});

		pm.registerClass(Tool.class, new Initialiser<Tool>() {
			@Override
			public Tool create() {
				return new MakePlacesExplicitTool(framework);
			}
		});
	}

	@Override
	public String getDescription() {
		return "Signal Transition Graphs";
	}
}
