package org.workcraft.plugins.shared;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.framework.Config;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName("Common")
public class CommonVisualSettings implements PersistentPropertyEditable, Plugin {
	private static LinkedList<PropertyDescriptor> properties;

	protected static double size = 1.0;
	protected static double strokeWidth = 0.1;

	protected static Color backgroundColor = Color.WHITE;
	protected static Color foregroundColor = Color.BLACK;
	protected static Color fillColor = Color.WHITE;

	public CommonVisualSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration("Base component size (cm)", "getSize", "setSize", double.class));
		properties.add(new PropertyDeclaration("Default stroke width (cm)", "getStrokeWidth", "setStrokeWidth", double.class));

		properties.add(new PropertyDeclaration("Editor background color", "getBackgroundColor", "setBackgroundColor", Color.class));
		properties.add(new PropertyDeclaration("Default foreground color", "getForegroundColor", "setForegroundColor", Color.class));
		properties.add(new PropertyDeclaration("Default fill color", "getFillColor", "setFillColor", Color.class));
	}

	public List<PropertyDescriptor> getPropertyDeclarations() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		size = config.getDouble("CommonVisualSettings.size", 1.0);
		strokeWidth = config.getDouble("CommonVisualSettings.strokeWidth", 0.1);
		backgroundColor = config.getColor("CommonVisualSettings.backgroundColor", Color.WHITE);
		foregroundColor = config.getColor("CommonVisualSettings.foregroundColor", Color.BLACK);
		fillColor = config.getColor("CommonVisualSettings.fillColor", Color.WHITE);
	}

	public void storePersistentProperties(Config config) {
		config.setDouble("CommonVisualSettings.size", size);
		config.setDouble("CommonVisualSettings.strokeWidth", strokeWidth);
		config.setColor("CommonVisualSettings.backgroundColor", backgroundColor);
		config.setColor("CommonVisualSettings.foregroundColor", foregroundColor);
		config.setColor("CommonVisualSettings.fillColor", fillColor);
	}

	public static Color getBackgroundColor() {
		return backgroundColor;
	}

	public static void setBackgroundColor(Color backgroundColor) {
		CommonVisualSettings.backgroundColor = backgroundColor;
	}

	public static Color getForegroundColor() {
		return foregroundColor;
	}

	public static void setForegroundColor(Color foregroundColor) {
		CommonVisualSettings.foregroundColor = foregroundColor;
	}

	public static Color getFillColor() {
		return fillColor;
	}

	public static void setFillColor(Color fillColor) {
		CommonVisualSettings.fillColor = fillColor;
	}

	public String getSection() {
		return "Visual";
	}

	public static double getSize() {
		return size;
	}

	public static void setSize(double size) {
		CommonVisualSettings.size = size;

	}

	public static double getStrokeWidth() {
		return strokeWidth;
	}

	public static void setStrokeWidth(double strokeWidth) {
		CommonVisualSettings.strokeWidth = strokeWidth;
	}



	public void addListener(PropertyChangeListener listener) {
	}


	public void firePropertyChanged(String propertyName) {
	}


	public void removeListener(PropertyChangeListener listener) {
	}

}
