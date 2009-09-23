package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.handshakestgbuilder.Process;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;

public abstract class ComponentStgBuilder <T> {
	@SuppressWarnings("unchecked")
	public void buildComponentStg(Component component, Map<String, Process> handshakes, StgBuilder builder)
	{
		buildStg((T)component, handshakes, builder);
	}

	public abstract void buildStg(T component, Map<String, Process> handshakes, StgBuilder builder);
}
