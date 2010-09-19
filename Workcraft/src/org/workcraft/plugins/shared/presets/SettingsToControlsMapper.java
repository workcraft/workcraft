package org.workcraft.plugins.shared.presets;

public interface SettingsToControlsMapper<T> {
	public void applySettingsToControls(T settings);
	public T getSettingsFromControls();
}
