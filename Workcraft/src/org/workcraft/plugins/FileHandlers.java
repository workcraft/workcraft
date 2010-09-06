package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Plugin;
import org.workcraft.PluginManager;
import org.workcraft.plugins.workspace.handlers.PunfUnfolding;
import org.workcraft.plugins.workspace.handlers.SystemOpen;
import org.workcraft.plugins.workspace.handlers.WorkcraftOpen;

public class FileHandlers implements Plugin {

	@Override
	public Class<?>[] getPluginClasses() {
		return new Class<?>[]
		                    {
								SystemOpen.class
		                    };
	}

	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(WorkcraftOpen.class, new Initialiser(){ @Override public Object create() { return new WorkcraftOpen(framework); }});
		p.registerClass(PunfUnfolding.class, new Initialiser(){ @Override public Object create() { return new PunfUnfolding(framework); }});
	}

}
