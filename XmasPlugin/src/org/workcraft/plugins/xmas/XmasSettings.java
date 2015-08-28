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

	private static final String keyBorderWidth = prefix + ".borderWidth";
	private static final String keyWireWidth  = prefix + ".wireWidth";
	private static final String keyJasonFileName  = prefix + ".jasonFileName";

	private static final double defaultBorderWidth = 0.06;
	private static final double defaultWireWidth = 0.04;
	private static final String defaultJasonFileName = "";

	private static double borderWidth = defaultBorderWidth;
	private static double wireWidth = defaultWireWidth;
	private static String jasonFileName = defaultJasonFileName;


	public XmasSettings() {
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
				this, "JSON conversion file", String.class, true, false, false) {
			protected void setter(XmasSettings object, String value) {
				XmasSettings.setJasonFileName(value);
			}
			protected String getter(XmasSettings object) {
				return XmasSettings.getJasonFileName();
			}
		});
	}

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setBorderWidth (config.getDouble(keyBorderWidth, defaultBorderWidth));
		setWireWidth(config.getDouble(keyWireWidth, defaultWireWidth));
		setJasonFileName(config.getString(keyJasonFileName, defaultJasonFileName));
	}

	@Override
	public void save(Config config) {
		config.setDouble(keyBorderWidth, getBorderWidth());
		config.setDouble(keyWireWidth, getWireWidth());
		config.set(keyJasonFileName, getJasonFileName());
	}

	@Override
	public String getSection() {
		return "Models";
	}

	@Override
	public String getName() {
		return "xMAS Circuit";
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

	public static String getJasonFileName() {
		return jasonFileName;
	}

	public static void setJasonFileName(String value) {
		jasonFileName = value;
	}

}
