package org.workcraft.plugins.stg;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class STGSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static boolean showInstanceNumbers = false;

	@Override
	public String getName() {
		return "Signal Transition Graph";
	}

	@Override
	public String getSection() {
		return "Visual";
	}

	@Override
	public void load(Config config) {
		showInstanceNumbers = config.getBoolean("STG.showInstanceNumbers", false);
	}

	@Override
	public void save(Config config) {
		config.setBoolean("STG.showInstanceNumbers", showInstanceNumbers);
	}

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public STGSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Show instance numbers", "getShowInstanceNumbers", "setShowInstanceNumbers", boolean.class));
	}

	public static void setShowInstanceNumbers(boolean showInstanceNumbers) {
		STGSettings.showInstanceNumbers = showInstanceNumbers;
	}

	public static boolean getShowInstanceNumbers() {
		return showInstanceNumbers;
	}

}
