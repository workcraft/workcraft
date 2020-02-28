package org.workcraft.presets;

public interface DataMapper<T> {
    void applyDataToControls(T settings);
    T getDataFromControls();
}
