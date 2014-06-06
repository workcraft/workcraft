package org.workcraft.plugins.cpog;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.cpog.EncoderSettings.generationMode;

public class ProgrammerUtilitySettings implements SettingsPage {
private static LinkedList<PropertyDescriptor> properties;

	private static final String commandKey = "Tools.encoder.command";
	private static final String solutionModeKey = "Tools.encoder.solutionMode";
	private static final String extraArgsKey = "Tools.encoder.args";

	private static String command = "scenco";
	private static generationMode genMode = generationMode.OPTIMAL_ENCODING;
	private static String extraArgs = "";

	public ProgrammerUtilitySettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<ProgrammerUtilitySettings, String>(
				this, "Scenco command", String.class) {
			protected void setter(ProgrammerUtilitySettings object, String value) {
				ProgrammerUtilitySettings.setCommand(value);
			}
			protected String getter(ProgrammerUtilitySettings object) {
				return ProgrammerUtilitySettings.getCommand();
			}
		});

		properties.add(new PropertyDeclaration<ProgrammerUtilitySettings, generationMode>(
				this, "Check mode", generationMode.class, generationMode.getChoice()) {
			protected void setter(ProgrammerUtilitySettings object, generationMode value) {
				ProgrammerUtilitySettings.setGenerationMode(value);
			}
			protected generationMode getter(ProgrammerUtilitySettings object) {
				return ProgrammerUtilitySettings.getGenerationMode();
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
		command = config.getString(commandKey, "scenco");
		genMode = config.getEnum(solutionModeKey, generationMode.class, generationMode.OPTIMAL_ENCODING);
		extraArgs = config.getString(extraArgsKey, "");
	}

	@Override
	public void save(Config config) {
		config.set(commandKey, command);
		config.setEnum(solutionModeKey, generationMode.class, genMode);
		config.set(extraArgsKey, extraArgs);
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
		ProgrammerUtilitySettings.command = value;
	}

	public static String getExtraArgs() {
		return extraArgs;
	}

	public static void setExtraArgs(String value) {
		ProgrammerUtilitySettings.extraArgs = value;
	}

	public static void setGenerationMode(generationMode value) {
		ProgrammerUtilitySettings.genMode = value;
	}

	public static generationMode getGenerationMode() {
		return genMode;
	}
}
