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
		return "Visual";
	}

	public SONSettings(){
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration(this, "Display node name", "getDisplayName", "setDisplayName", Boolean.class));

		properties.add(new PropertyDeclaration(this, "Erroneous node color(relation)", "getRelationErrColor", "setRelationErrColor", Color.class));
		properties.add(new PropertyDeclaration(this, "Erroneous node color(cycle)", "getCyclePathColor", "setCyclePathColor", Color.class));
		properties.add(new PropertyDeclaration(this, "Erroneous connection color", "getConnectionErrColor", "setConnectionErrColor", Color.class));
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
