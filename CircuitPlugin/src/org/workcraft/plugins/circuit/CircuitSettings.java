package org.workcraft.plugins.circuit;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;


public class CircuitSettings implements Settings {
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "CircuitSettings";

	private static final String keyShowContacts  = prefix + ".showContacts";
	private static final String keyActiveWireColor  = prefix + ".activeWireColor";
	private static final String keyInactiveWireColor  = prefix + ".inactiveWireColor";
	private static final String keyBorderWidth  = prefix + ".borderWidth";
	private static final String keyWireWidth  = prefix + ".wireWidth";
	private static final String keySimplifyStg = prefix + ".simplifyStg";

	private static final boolean defaultShowContacts = false;
	private static final Color defaultActiveWireColor = new Color(1.0f, 0.0f, 0.0f);
	private static final Color defaultInactiveWireColor = new Color(0.0f, 0.0f, 1.0f);
	private static final Double defaultBorderWidth = 0.06;
	private static final Double defaultWireWidth = 0.04;
	private static final boolean defaultSimplifyStg = true;

	private static boolean showContacts = defaultShowContacts;
	private static Color activeWireColor = defaultActiveWireColor;
	private static Color inactiveWireColor = defaultInactiveWireColor;
	private static Double borderWidth = defaultBorderWidth;
	private static Double wireWidth = defaultWireWidth;
	private static boolean simplifyStg = defaultSimplifyStg;

	public CircuitSettings() {
		properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
				this, "Show contacts", Boolean.class) {
			protected void setter(CircuitSettings object, Boolean value) {
				CircuitSettings.setShowContacts(value);
			}
			protected Boolean getter(CircuitSettings object) {
				return CircuitSettings.getShowContacts();
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
				this, "Border width", Double.class) {
			protected void setter(CircuitSettings object, Double value) {
				CircuitSettings.setBorderWidth(value);
			}
			protected Double getter(CircuitSettings object) {
				return CircuitSettings.getBorderWidth();
			}
		});

		properties.add(new PropertyDeclaration<CircuitSettings, Double>(
				this, "Wire width", Double.class) {
			protected void setter(CircuitSettings object, Double value) {
				CircuitSettings.setWireWidth(value);
			}
			protected Double getter(CircuitSettings object) {
				return CircuitSettings.getWireWidth();
			}
		});

		properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
				this, "Simplify generated circuit STG", Boolean.class) {
			protected void setter(CircuitSettings object, Boolean value) {
				CircuitSettings.setSimplifyStg(value);
			}
			protected Boolean getter(CircuitSettings object) {
				return CircuitSettings.getSimplifyStg();
			}
		});
	}

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public String getSection() {
		return "Models";
	}

	@Override
	public String getName() {
		return "Digital Circuit";
	}

	@Override
	public void load(Config config) {
		setShowContacts(config.getBoolean(keyShowContacts, defaultShowContacts));
		setActiveWireColor(config.getColor(keyActiveWireColor, defaultActiveWireColor));
		setInactiveWireColor(config.getColor(keyInactiveWireColor, defaultInactiveWireColor));
		setBorderWidth(config.getDouble(keyBorderWidth, defaultBorderWidth));
		setWireWidth(config.getDouble(keyWireWidth, defaultWireWidth));
		setSimplifyStg(config.getBoolean(keySimplifyStg, defaultSimplifyStg));
	}

	@Override
	public void save(Config config) {
		config.setBoolean(keyShowContacts, getShowContacts());
		config.setColor(keyActiveWireColor, getActiveWireColor());
		config.setColor(keyInactiveWireColor, getInactiveWireColor());
		config.setDouble(keyBorderWidth, getBorderWidth());
		config.setDouble(keyWireWidth, getWireWidth());
		config.setBoolean(keySimplifyStg, getSimplifyStg());
	}

	public static boolean getShowContacts() {
		return showContacts;
	}

	public static void setShowContacts(boolean value) {
		showContacts = value;
	}

	public static Color getActiveWireColor() {
		return activeWireColor;
	}

	public static void setActiveWireColor(Color value) {
		activeWireColor = value;
	}

	public static Color getInactiveWireColor() {
		return inactiveWireColor;
	}

	public static void setInactiveWireColor(Color value) {
		inactiveWireColor = value;
	}

	public static double getBorderWidth() {
		return borderWidth;
	}

	public static void setBorderWidth(double value) {
		borderWidth = value;
	}

	public static double getWireWidth() {
		return wireWidth;
	}

	public static void setWireWidth(double value) {
		wireWidth = value;
	}

	public static boolean getSimplifyStg() {
		return simplifyStg;
	}

	public static void setSimplifyStg(boolean value) {
		simplifyStg = value;
	}

}
