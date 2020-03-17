package org.workcraft.plugins;

import java.util.Collection;

public interface PluginProvider {
    <T> Collection<PluginInfo<? extends T>> getPlugins(Class<T> interfaceType);
}
