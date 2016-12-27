package org.workcraft;

import java.util.Collection;

import org.workcraft.plugins.PluginInfo;

public interface PluginProvider {
    <T> Collection<PluginInfo<? extends T>> getPlugins(Class<T> interfaceType);
}
