package org.workcraft;


public interface PluginProvider {
	public PluginInfo[] getPluginsImplementing(String interfaceName);
}