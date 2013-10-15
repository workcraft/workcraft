package org.workcraft.plugins.son;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class SONSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;
	private static boolean displayName = false;
	private static Color relationErrColor = new Color(255, 204, 204);
	private static Color cyclePathColor = new Color(255, 102, 102);
	private static Color connectionErrColor = new  Color(255, 102, 102);

	@Override
	public String getName() {
		return "Structured Occurrence Nets";
	}

	public String getSection() {
		return "Models";
	}

	public SONSettings(){
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<SONSettings, Boolean>(
				this, "Display node name", Boolean.class) {
			protected void setter(SONSettings object, Boolean value) {
				SONSettings.setDisplayName(value);
			}
			protected Boolean getter(SONSettings object) {
				return SONSettings.getDisplayName();
			}
		});

		properties.add(new PropertyDeclaration<SONSettings, Color>(
				this, "Erroneous node color(relation)", Color.class) {
			protected void setter(SONSettings object, Color value) {
				SONSettings.setRelationErrColor(value);
			}
			protected Color getter(SONSettings object) {
				return SONSettings.getRelationErrColor();
			}
		});

		properties.add(new PropertyDeclaration<SONSettings, Color>(
				this, "Erroneous node color(cycle)", Color.class) {
			protected void setter(SONSettings object, Color value) {
				SONSettings.setCyclePathColor(value);
			}
			protected Color getter(SONSettings object) {
				return SONSettings.getCyclePathColor();
			}
		});

		properties.add(new PropertyDeclaration<SONSettings, Color>(
				this, "Erroneous connection color", Color.class) {
			protected void setter(SONSettings object, Color value) {
				SONSettings.setConnectionErrColor(value);
			}
			protected Color getter(SONSettings object) {
				return SONSettings.getConnectionErrColor();
			}
		});
	}

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void save(Config config) {
		config.setBoolean("SONSettings.displayName", displayName);

		config.setColor("SONSettings.relationErrColor", relationErrColor);
		config.setColor("SONSettings.cyclePathColor", cyclePathColor);
		config.setColor("SONSettings.connectionErrColor", connectionErrColor);
	}

	@Override
	public void load(Config config) {
		displayName = config.getBoolean("SONSettings.displayName", false);

		relationErrColor = config.getColor("SONSettings.relationErrColor", new Color(255, 204, 204));
		cyclePathColor = config.getColor("SONSettings.cyclePathColor", new Color(255, 102, 102));
		connectionErrColor = config.getColor("SONSettings.connectionErrColor", new Color(255, 102, 102));
	}

	public static void setDisplayName(Boolean displayName){
		SONSettings.displayName = displayName;
	}

	public static boolean getDisplayName(){
		return displayName;
	}

	public static void setRelationErrColor(Color color){
		SONSettings.relationErrColor = color;
	}

	public static Color getRelationErrColor(){
		return relationErrColor;
	}

	public static void setCyclePathColor(Color color){
		SONSettings.cyclePathColor = color;
	}

	public static Color getCyclePathColor(){
		return cyclePathColor;
	}

	public static void setConnectionErrColor(Color color){
		SONSettings.connectionErrColor = color;
	}

	public static Color getConnectionErrColor(){
		return connectionErrColor;
	}
}
