package org.workcraft.plugins.circuit;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;


public class CircuitSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static boolean showContacts = true;
	private static boolean showArrows = true;
	private static Color activeWireColor = new Color(1.0f, 0.0f, 0.0f);
	private static Double componentBorderWidth=1.0;
	private static Double circuitWireWidth=1.0;
	private static SolutionMode checkMode = SolutionMode.FIRST;

	public static double getComponentBorderWidth() {
		return componentBorderWidth;
	}

	public static void setComponentBorderWidth(double componentBorderWidth) {
		CircuitSettings.componentBorderWidth = componentBorderWidth;
	}

	public static double getCircuitWireWidth() {
		return circuitWireWidth;
	}

	public static void setCircuitWireWidth(double circuitWireWidth) {
		CircuitSettings.circuitWireWidth = circuitWireWidth;
	}

	public static boolean getShowContacts() {
		return showContacts;
	}

	public static void setShowContacts(boolean showContacts) {
		CircuitSettings.showContacts = showContacts;
	}

	public static boolean getShowArrows() {
		return CircuitSettings.showArrows;
	}

	public static void setShowArrows(boolean showArrows) {
		CircuitSettings.showArrows = showArrows;
	}

	public static Color getActiveWireColor() {
		return CircuitSettings.activeWireColor;
	}

	public static void setActiveWireColor(Color activeWireColor) {
		CircuitSettings.activeWireColor = activeWireColor;
	}

	public static Color getInactiveWireColor() {
		return inactiveWireColor;
	}

	public static void setInactiveWireColor(Color inactiveWireColor) {
		CircuitSettings.inactiveWireColor = inactiveWireColor;
	}

	private static Color inactiveWireColor = new Color(0.0f, 0.0f, 1.0f);

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public CircuitSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration(this, "Show contacts", "getShowContacts", "setShowContacts", boolean.class));
		properties.add(new PropertyDeclaration(this, "Show arrows", "getShowArrows", "setShowArrows", boolean.class));

		properties.add(new PropertyDeclaration(this, "Active wire", "getActiveWireColor", "setActiveWireColor", Color.class));
		properties.add(new PropertyDeclaration(this, "Inactive wire", "getInactiveWireColor", "setInactiveWireColor", Color.class));

		properties.add(new PropertyDeclaration(this, "Component width", "getComponentBorderWidth", "setComponentBorderWidth", double.class));
		properties.add(new PropertyDeclaration(this, "Wire width", "getCircuitWireWidth", "setCircuitWireWidth", double.class));

		LinkedHashMap<String, Object> modes = new LinkedHashMap<String, Object>();

		modes.put("First", SolutionMode.FIRST);
		modes.put("Minimal cost", SolutionMode.MINIMUM_COST);
		modes.put("First 10 solutions", SolutionMode.ALL);

		properties.add(new PropertyDeclaration(this, "Check mode", "getCheckMode", "setCheckMode", SolutionMode.class, modes));
	}

	@Override
	public String getName() {
		return "Digital Circuit";
	}

	@Override
	public String getSection() {
		return "Visual";
	}

	@Override
	public void load(Config config) {
		showContacts = config.getBoolean("CircuitSettings.showContacts", true);
		showArrows = config.getBoolean("CircuitSettings.showArrows", true);

		activeWireColor = config.getColor("CircuitSettings.activeWireColor", new Color(1.0f, 0.0f, 0.0f));
		inactiveWireColor = config.getColor("CircuitSettings.inactiveWireColor", new Color(0.0f, 0.0f, 1.0f));

		componentBorderWidth  = config.getDouble("CircuitSettings.componentBorderWidth", 0.06);
		circuitWireWidth = config.getDouble("CircuitSettings.circuitWireWidth", 0.04);

		checkMode = config.getEnum("CircuitSettings.circuitCheckMode", SolutionMode.class, SolutionMode.FIRST);
	}

	@Override
	public void save(Config config) {
		config.setBoolean("CircuitSettings.showContacts", showContacts);
		config.setBoolean("CircuitSettings.showArrows", showArrows);

		config.setColor("CircuitSettings.activeWireColor", activeWireColor);
		config.setColor("CircuitSettings.inactiveWireColor", inactiveWireColor);

		config.setDouble("CircuitSettings.componentBorderWidth", componentBorderWidth);
		config.setDouble("CircuitSettings.circuitWireWidth", circuitWireWidth);

		config.setEnum("CircuitSettings.circuitCheckMode", SolutionMode.class, checkMode);
	}

	public static void setCheckMode(SolutionMode checkMode) {
		CircuitSettings.checkMode = checkMode;
	}

	public static SolutionMode getCheckMode() {
		return checkMode;
	}

}
