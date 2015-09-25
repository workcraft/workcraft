package org.workcraft.plugins.xmas;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;


public class XmasSettings implements Settings {
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "XmasSettings";

	private static final String keyShowContacts  = prefix + ".showContacts";
	private static final String keyBorderWidth = prefix + ".borderWidth";
	private static final String keyWireWidth  = prefix + ".wireWidth";
	private static final String keyVxmDirectory  = prefix + ".vxmDirectory";

	private static final boolean defaultShowContacts = false;
	private static final double defaultBorderWidth = 0.06;
	private static final double defaultWireWidth = 0.06;
	private static final String defaultVxmDirectory = "tools/vxm/";

	private static boolean showContacts = defaultShowContacts;
	private static double borderWidth = defaultBorderWidth;
	private static double wireWidth = defaultWireWidth;
	private static String vxmDirectory = defaultVxmDirectory;


	public XmasSettings() {
		properties.add(new PropertyDeclaration<XmasSettings, Boolean>(
				this, "Show contacts", Boolean.class, true, false, false) {
			protected void setter(XmasSettings object, Boolean value) {
				setShowContacts(value);
			}
			protected Boolean getter(XmasSettings object) {
				return getShowContacts();
			}
		});

		properties.add(new PropertyDeclaration<XmasSettings, Double>(
				this, "Border width", Double.class, true, false, false) {
			protected void setter(XmasSettings object, Double value) {
				XmasSettings.setBorderWidth(value);
			}
			protected Double getter(XmasSettings object) {
				return XmasSettings.getBorderWidth();
			}
		});

		properties.add(new PropertyDeclaration<XmasSettings, Double>(
				this, "Wire width", Double.class, true, false, false) {
			protected void setter(XmasSettings object, Double value) {
				XmasSettings.setWireWidth(value);
			}
			protected Double getter(XmasSettings object) {
				return XmasSettings.getWireWidth();
			}
		});

		properties.add(new PropertyDeclaration<XmasSettings, String>(
				this, "VXM working directory", String.class, true, false, false) {
			protected void setter(XmasSettings object, String value) {
				XmasSettings.setVxmDirectory(value);
			}
			protected String getter(XmasSettings object) {
				return XmasSettings.getVxmDirectory();
			}
		});
	}

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setShowContacts(config.getBoolean(keyShowContacts, defaultShowContacts));
		setBorderWidth (config.getDouble(keyBorderWidth, defaultBorderWidth));
		setWireWidth(config.getDouble(keyWireWidth, defaultWireWidth));
		setVxmDirectory(config.getString(keyVxmDirectory, defaultVxmDirectory));
	}

	@Override
	public void save(Config config) {
		config.setBoolean(keyShowContacts, getShowContacts());
		config.setDouble(keyBorderWidth, getBorderWidth());
		config.setDouble(keyWireWidth, getWireWidth());
		config.set(keyVxmDirectory, getVxmDirectory());
	}

	@Override
	public String getSection() {
		return "Models";
	}

	@Override
	public String getName() {
		return "xMAS Circuit";
	}

	public static boolean getShowContacts() {
		return showContacts;
	}

	public static void setShowContacts(boolean value) {
		showContacts = value;
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

	public static String getVxmDirectory() {
		return vxmDirectory;
	}

	public static void setVxmDirectory(String value) {
		vxmDirectory = value;
	}

}
