package org.workcraft.plugins.circuit;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;


public class CircuitSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static boolean showContacts = true;
	private static boolean showArrows = true;
	private static Color activeWireColor = new Color(1.0f, 0.0f, 0.0f);
	private static Double componentBorderWidth=1.0;
	private static Double circuitWireWidth=1.0;

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

		properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
				this, "Show contacts", Boolean.class) {
			protected void setter(CircuitSettings object, Boolean value) {
				CircuitSettings.setShowContacts(value);
			}
			protected Boolean getter(CircuitSettings object) {
				return CircuitSettings.getShowContacts();
			}
		});

		properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
				this, "Show arrows", Boolean.class) {
			protected void setter(CircuitSettings object, Boolean value) {
				CircuitSettings.setShowArrows(value);
			}
			protected Boolean getter(CircuitSettings object) {
				return CircuitSettings.getShowArrows();
			}
		});

		properties.add(new PropertyDeclaration<CircuitSettings, Color>(
				this, "Active wire", Color.class) {
			protected void setter(CircuitSettings object, Color value) {
				CircuitSettings.setActiveWireColor(value);
			}
			protected Color getter(CircuitSettings object) {
				return CircuitSettings.getActiveWireColor();
			}
		});

		properties.add(new PropertyDeclaration<CircuitSettings, Color>(
				this, "Inactive wire", Color.class) {
			protected void setter(CircuitSettings object, Color value) {
				CircuitSettings.setInactiveWireColor(value);
			}
			protected Color getter(CircuitSettings object) {
				return CircuitSettings.getInactiveWireColor();
			}
		});

		properties.add(new PropertyDeclaration<CircuitSettings, Double>(
				this, "Component width", Double.class) {
			protected void setter(CircuitSettings object, Double value) {
				CircuitSettings.setComponentBorderWidth(value);
			}
			protected Double getter(CircuitSettings object) {
				return CircuitSettings.getComponentBorderWidth();
			}
		});

		properties.add(new PropertyDeclaration<CircuitSettings, Double>(
				this, "Wire width", Double.class) {
			protected void setter(CircuitSettings object, Double value) {
				CircuitSettings.setCircuitWireWidth(value);
			}
			protected Double getter(CircuitSettings object) {
				return CircuitSettings.getCircuitWireWidth();
			}
		});
	}

	@Override
	public String getName() {
		return "Digital Circuit";
	}

	@Override
	public String getSection() {
		return "Models";
	}

	@Override
	public void load(Config config) {
		showContacts = config.getBoolean("CircuitSettings.showContacts", true);
		showArrows = config.getBoolean("CircuitSettings.showArrows", true);

		activeWireColor = config.getColor("CircuitSettings.activeWireColor", new Color(1.0f, 0.0f, 0.0f));
		inactiveWireColor = config.getColor("CircuitSettings.inactiveWireColor", new Color(0.0f, 0.0f, 1.0f));

		componentBorderWidth  = config.getDouble("CircuitSettings.componentBorderWidth", 0.06);
		circuitWireWidth = config.getDouble("CircuitSettings.circuitWireWidth", 0.04);
	}

	@Override
	public void save(Config config) {
		config.setBoolean("CircuitSettings.showContacts", showContacts);
		config.setBoolean("CircuitSettings.showArrows", showArrows);

		config.setColor("CircuitSettings.activeWireColor", activeWireColor);
		config.setColor("CircuitSettings.inactiveWireColor", inactiveWireColor);

		config.setDouble("CircuitSettings.componentBorderWidth", componentBorderWidth);
		config.setDouble("CircuitSettings.circuitWireWidth", circuitWireWidth);
	}

}
