package org.workcraft.plugins.son;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class SONSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;
	private static Color relationErrColor = new Color(255, 204, 204);
	private static Color cyclePathColor = new Color(255, 102, 102);
	private static Color connectionErrColor = new  Color(255, 102, 102);
	private static Positioning errLabelPositioning = Positioning.BOTTOM;
	private static Color errLabelColor = Color.GREEN.darker();

	@Override
	public String getName() {
		return "Structured Occurrence Nets";
	}

	public String getSection() {
		return "Models";
	}

	public SONSettings(){
		properties = new LinkedList<PropertyDescriptor>();

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

		properties.add(new PropertyDeclaration<SONSettings, Positioning>(
				this, "Error label positioning", Positioning.class, Positioning.getChoice()) {
			protected void setter(SONSettings object, Positioning value) {
				SONSettings.setErrLabelPositioning(value);
			}
			protected Positioning getter(SONSettings object) {
				return SONSettings.getErrLabelPositioning();
			}
		});

		properties.add(new PropertyDeclaration<SONSettings, Color>(
				this, "Error label color", Color.class) {
			protected void setter(SONSettings object, Color value) {
				SONSettings.setErrLabelColor(value);
			}
			protected Color getter(SONSettings object) {
				return SONSettings.getErrLabelColor();
			}
		});
	}

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void save(Config config) {
		config.setColor("SONSettings.relationErrColor", relationErrColor);
		config.setColor("SONSettings.cyclePathColor", cyclePathColor);
		config.setColor("SONSettings.connectionErrColor", connectionErrColor);

		config.setTextPositioning("SONSettings.errLabelPositioning", errLabelPositioning);
		config.setColor("SONSettings.errLabelColor", errLabelColor);
	}

	@Override
	public void load(Config config) {
		relationErrColor = config.getColor("SONSettings.relationErrColor", new Color(255, 204, 204));
		cyclePathColor = config.getColor("SONSettings.cyclePathColor", new Color(255, 102, 102));
		connectionErrColor = config.getColor("SONSettings.connectionErrColor", new Color(255, 102, 102));

		errLabelPositioning = config.getTextPositioning("SONSettings.errorPositioning", Positioning.BOTTOM);
		errLabelColor = config.getColor("SONSettings.errLabelColor", Color.GREEN.darker());
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

	public static Positioning getErrLabelPositioning() {
		return errLabelPositioning;
	}

	public static void setErrLabelPositioning(Positioning value) {
		SONSettings.errLabelPositioning = value;
	}

	public static Color getErrLabelColor() {
		return errLabelColor;
	}

	public static void setErrLabelColor(Color value) {
		SONSettings.errLabelColor = value;
	}
}
