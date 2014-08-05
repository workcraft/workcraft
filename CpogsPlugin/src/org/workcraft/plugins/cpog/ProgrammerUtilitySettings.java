package org.workcraft.plugins.cpog;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;

public class ProgrammerUtilitySettings implements Settings {
	private static LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "Tools.encoder";

	private static final String keyCommand = prefix + ".command";
	private static final String keySolutionMode = prefix + ".solutionMode";
	private static final String keyExtraArgs = prefix + ".args";

	private static final String defaultCommand = "scenco";
	private static final GenerationMode defaultSolutionMode = GenerationMode.OPTIMAL_ENCODING;
	private static final String defaultExtraArgs = "";

	private static String command = defaultCommand;
	private static GenerationMode solutionMode = defaultSolutionMode;
	private static String extraArgs = defaultExtraArgs;

	public ProgrammerUtilitySettings() {
		properties.add(new PropertyDeclaration<ProgrammerUtilitySettings, String>(
				this, "Scenco command", String.class) {
			protected void setter(ProgrammerUtilitySettings object, String value) {
				ProgrammerUtilitySettings.setCommand(value);
			}
			protected String getter(ProgrammerUtilitySettings object) {
				return ProgrammerUtilitySettings.getCommand();
			}
		});

		properties.add(new PropertyDeclaration<ProgrammerUtilitySettings, GenerationMode>(
				this, "Check mode", GenerationMode.class, GenerationMode.getChoice()) {
			protected void setter(ProgrammerUtilitySettings object, GenerationMode value) {
				ProgrammerUtilitySettings.setSolutionMode(value);
			}
			protected GenerationMode getter(ProgrammerUtilitySettings object) {
				return ProgrammerUtilitySettings.getSolutionMode();
			}
		});

		properties.add(new PropertyDeclaration<ProgrammerUtilitySettings, String>(
				this, "Scenco additional arguments", String.class) {
			protected void setter(ProgrammerUtilitySettings object, String value) {
				ProgrammerUtilitySettings.setExtraArgs(value);
			}
			protected String getter(ProgrammerUtilitySettings object) {
				return ProgrammerUtilitySettings.getExtraArgs();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setCommand(config.getString(keyCommand, defaultCommand));
		setSolutionMode(config.getEnum(keySolutionMode, GenerationMode.class, defaultSolutionMode));
		setExtraArgs(config.getString(keyExtraArgs, defaultExtraArgs));
	}

	@Override
	public void save(Config config) {
		config.set(keyCommand, getCommand());
		config.setEnum(keySolutionMode, GenerationMode.class, getSolutionMode());
		config.set(keyExtraArgs, getExtraArgs());
	}

	@Override
	public String getSection() {
		return "External tools";
	}

	@Override
	public String getName() {
		return "Scenco";
	}

	public static String getCommand() {
		return command;
	}

	public static void setCommand(String value) {
		command = value;
	}

	public static String getExtraArgs() {
		return extraArgs;
	}

	public static void setExtraArgs(String value) {
		extraArgs = value;
	}

	public static void setSolutionMode(GenerationMode value) {
		solutionMode = value;
	}

	public static GenerationMode getSolutionMode() {
		return solutionMode;
	}
}
