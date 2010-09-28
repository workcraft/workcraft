package org.workcraft.plugins;

public interface PluginInfo<T> {
	public T newInstance();
	public T getSingleton();
}
