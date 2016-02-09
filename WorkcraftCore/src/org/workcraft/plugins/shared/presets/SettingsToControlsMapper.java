package org.workcraft.plugins.shared.presets;

public interface SettingsToControlsMapper<T> {
    void applySettingsToControls(T settings);
    T getSettingsFromControls();
}
