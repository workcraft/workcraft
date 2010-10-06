package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Initialiser;
import org.workcraft.Module;
import org.workcraft.PluginManager;
import org.workcraft.plugins.workspace.handlers.SystemOpen;
import org.workcraft.plugins.workspace.handlers.WorkcraftOpen;
import org.workcraft.workspace.FileHandler;

public class FileHandlers implements Module {
	@Override
	public void init(final Framework framework) {
		final PluginManager p = framework.getPluginManager();
		p.registerClass(FileHandler.class, new Initialiser<FileHandler>(){ @Override public FileHandler create() { return new WorkcraftOpen(framework); }});
		p.registerClass(FileHandler.class, SystemOpen.class);
		//p.registerClass(PunfUnfolding.class, new Initialiser(){ @Override public Object create() { return new PunfUnfolding(framework); }});
	}

}
