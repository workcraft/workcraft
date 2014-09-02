package org.workcraft.plugins.son;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class SONSettings implements Settings {
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "SONSettings";

	private static final String keyRelationErrColor = prefix + ".relationErrColor";
	private static final String keyCyclePathColor = prefix + ".cyclePathColor";
	private static final String keyConnectionErrColor = prefix + ".connectionErrColor";
	private static final String keyErrLabelColor = prefix + ".errLabelColor";
	private static final String keyBlockColor = prefix + ".blockColor";
	private static final String keyErrLabelPositioning = prefix + ".errLabelPositioning";

	private static final Color defaultRelationErrColor = new Color(255, 204, 204);
	private static final Color defaultCyclePathColor = new Color(255, 102, 102);
	private static final Color defaultConnectionErrColor = new  Color(255, 102, 102);
	private static final Color defaultErrLabelColor = Color.GREEN.darker();
	private static final Color defaultBlockColor = new  Color(255, 255, 255, 0);
	private static final Positioning defaultErrLabelPositioning = Positioning.BOTTOM;

	private static Color relationErrColor = defaultRelationErrColor;
	private static Color cyclePathColor = defaultCyclePathColor;
	private static Color connectionErrColor = defaultConnectionErrColor;
	private static Color errLabelColor = defaultErrLabelColor;
	private static Color blockColor = defaultBlockColor;
	private static Positioning errLabelPositioning = defaultErrLabelPositioning;

	public SONSettings(){
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

		properties.add(new PropertyDeclaration<SONSettings, Color>(
				this, "Default Block color", Color.class) {
			protected void setter(SONSettings object, Color value) {
				SONSettings.setBlockColor(value);
			}
			protected Color getter(SONSettings object) {
				return SONSettings.getBlockColor();
			}
		});
	}

	@Override
	public Collection<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setRelationErrColor(config.getColor(keyRelationErrColor, defaultRelationErrColor));
		setCyclePathColor(config.getColor(keyCyclePathColor, defaultCyclePathColor));
		setConnectionErrColor(config.getColor(keyConnectionErrColor, defaultConnectionErrColor));
		setErrLabelPositioning(config.getTextPositioning(keyErrLabelPositioning, defaultErrLabelPositioning));
		setErrLabelColor(config.getColor(keyErrLabelColor, defaultErrLabelColor));
	}

	@Override
	public void save(Config config) {
		config.setColor(keyRelationErrColor, getRelationErrColor());
		config.setColor(keyCyclePathColor, getCyclePathColor());
		config.setColor(keyConnectionErrColor, getConnectionErrColor());
		config.setColor(keyBlockColor, getBlockColor());
		config.setTextPositioning(keyErrLabelPositioning, getErrLabelPositioning());
		config.setColor(keyErrLabelColor, getErrLabelColor());
	}

	@Override
	public String getSection() {
		return "Models";
	}

	@Override
	public String getName() {
		return "Structured Occurrence Nets";
	}

	public static void setRelationErrColor(Color value){
		relationErrColor = value;
	}

	public static Color getRelationErrColor(){
		return relationErrColor;
	}

	public static void setCyclePathColor(Color value){
		cyclePathColor = value;
	}

	public static Color getCyclePathColor(){
		return cyclePathColor;
	}

	public static void setConnectionErrColor(Color value){
		connectionErrColor = value;
	}

	public static Color getConnectionErrColor(){
		return connectionErrColor;
	}

	public static Positioning getErrLabelPositioning() {
		return errLabelPositioning;
	}

	public static void setErrLabelPositioning(Positioning value) {
		errLabelPositioning = value;
	}

	public static Color getErrLabelColor() {
		return errLabelColor;
	}

	public static void setErrLabelColor(Color value) {
		errLabelColor = value;
	}

	public static Color getBlockColor() {
		return blockColor;
	}

	public static void setBlockColor(Color value) {
		blockColor = value;
	}
}
