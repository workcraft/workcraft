package org.workcraft.plugins.layout;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.framework.Config;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.gui.propertyeditor.PersistentPropertyEditable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;

@DisplayName ("Random")
public class RandomLayoutSettings implements PersistentPropertyEditable, Plugin {
	protected static double startX = 0;
	protected static double startY = 0;
	protected static double rangeX = 30;
	protected static double rangeY = 30;


	private static LinkedList<PropertyDescriptor> properties;

	public RandomLayoutSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration("Start X", "getStartX", "setStartX", double.class));
		properties.add(new PropertyDeclaration("Start Y", "getStartY", "setStartY", double.class));
		properties.add(new PropertyDeclaration("Range X", "getRangeX", "setRangeX", double.class));
		properties.add(new PropertyDeclaration("Range Y", "getRangeY", "setRangeY", double.class));

	}
	public List<PropertyDescriptor> getPropertyDeclarations() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		startX = config.getDouble("RandomLayout.startX", 0);
		startY = config.getDouble("RandomLayout.startY", 0);
		rangeX = config.getDouble("RandomLayout.rangeX", 30);
		rangeY = config.getDouble("RandomLayout.rangeY", 30);
	}

	public void storePersistentProperties(Config config) {
		config.setDouble("RandomLayout.startX", startX);
		config.setDouble("RandomLayout.startY", startY);
		config.setDouble("RandomLayout.rangeX", rangeX);
		config.setDouble("RandomLayout.rangeY", rangeY);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}

	public void firePropertyChanged(String propertyName) {
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

	public String getSection() {
		return "Layout";
	}
	public static double getStartX() {
		return startX;
	}
	public static void setStartX(double startX) {
		RandomLayoutSettings.startX = startX;
	}
	public static double getStartY() {
		return startY;
	}
	public static void setStartY(double startY) {
		RandomLayoutSettings.startY = startY;
	}
	public static double getRangeX() {
		return rangeX;
	}
	public static void setRangeX(double rangeX) {
		RandomLayoutSettings.rangeX = rangeX;
	}
	public static double getRangeY() {
		return rangeY;
	}
	public static void setRangeY(double rangeY) {
		RandomLayoutSettings.rangeY = rangeY;
	}
}
