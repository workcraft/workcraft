package org.workcraft.presets;

public interface SettingsToControlsMapper<T> {
    void applySettingsToControls(T settings);
    T getSettingsFromControls();
}
