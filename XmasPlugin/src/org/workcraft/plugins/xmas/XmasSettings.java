package org.workcraft.plugins.xmas;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;


public class XmasSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static boolean showArrows = true;
	private static Double borderWidth=0.1;
	private static Double wireWidth=0.04;
	private static String jasonFileName = "";

	public static double getBorderWidth() {
		return borderWidth;
	}

	public static void setBorderWidth(double borderWidth) {
		XmasSettings.borderWidth = borderWidth;
	}

	public static double getWireWidth() {
		return wireWidth;
	}

	public static void setWireWidth(double wireWidth) {
		XmasSettings.wireWidth = wireWidth;
	}

	public static boolean getShowArrows() {
		return XmasSettings.showArrows;
	}

	public static void setShowArrows(boolean showArrows) {
		XmasSettings.showArrows = showArrows;
	}

	public static String getJasonFileName() {
		return XmasSettings.jasonFileName;
	}

	public static void setJasonFileName(String value) {
		XmasSettings.jasonFileName = value;
	}

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public XmasSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<XmasSettings, Boolean>(
				this, "Show arrows", Boolean.class) {
			protected void setter(XmasSettings object, Boolean value) {
				XmasSettings.setShowArrows(value);
			}
			protected Boolean getter(XmasSettings object) {
				return XmasSettings.getShowArrows();
			}
		});

		properties.add(new PropertyDeclaration<XmasSettings, Double>(
				this, "Border width", Double.class) {
			protected void setter(XmasSettings object, Double value) {
				XmasSettings.setBorderWidth(value);
			}
			protected Double getter(XmasSettings object) {
				return XmasSettings.getBorderWidth();
			}
		});

		properties.add(new PropertyDeclaration<XmasSettings, Double>(
				this, "Wire width", Double.class) {
			protected void setter(XmasSettings object, Double value) {
				XmasSettings.setWireWidth(value);
			}
			protected Double getter(XmasSettings object) {
				return XmasSettings.getWireWidth();
			}
		});

		properties.add(new PropertyDeclaration<XmasSettings, String>(
				this, "JSON conversion file", String.class) {
			protected void setter(XmasSettings object, String value) {
				XmasSettings.setJasonFileName(value);
			}
			protected String getter(XmasSettings object) {
				return XmasSettings.getJasonFileName();
			}
		});
	}

	@Override
	public String getName() {
		return "xMAS Circuit";
	}

	@Override
	public String getSection() {
		return "Models";
	}

	@Override
	public void load(Config config) {
		showArrows = config.getBoolean("XmasSettings.showArrows", true);

		borderWidth  = config.getDouble("XmasSettings.borderWidth", 0.1);
		wireWidth = config.getDouble("XmasSettings.wireWidth", 0.04);

		jasonFileName = config.getString("XmasSettings.jasonFileName", "");
	}

	@Override
	public void save(Config config) {
		config.setBoolean("XmasSettings.showArrows", showArrows);

		config.setDouble("XmasSettings.borderWidth", borderWidth);
		config.setDouble("XmasSettings.wireWidth", wireWidth);

		config.set("XmasSettings.jasonFileName", jasonFileName);
	}

}
