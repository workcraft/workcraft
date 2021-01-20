package org.workcraft.plugins;

public class PluginInfo<T> {

    private final Initialiser<? extends T> initialiser;
    private final boolean singleton;
    private T instance;

    public PluginInfo(Initialiser<? extends T> initialiser, boolean singleton) {
        this.initialiser = initialiser;
        this.singleton = singleton;
    }

    public T getInstance() {
        if (!singleton || (instance == null)) {
            instance = initialiser.create();
        }
        return instance;
    }

}
