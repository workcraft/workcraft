package org.workcraft.plugins;

public interface PluginInfo<T> {
    T newInstance();
    T getSingleton();
}
