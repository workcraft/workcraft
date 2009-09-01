package org.workcraft.framework.plugins;


public interface PluginProvider {
	public PluginInfo[] getPluginsImplementing(String interfaceName);
}