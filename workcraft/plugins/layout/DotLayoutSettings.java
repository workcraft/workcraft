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

@DisplayName ("Dot")
public class DotLayoutSettings implements PersistentPropertyEditable, Plugin {
	protected static double dotPositionScaleFactor = 0.02;
	protected static String tmpGraphFilePath = "tmp.dot";
	protected static String dotCommand = "dot";

	private static LinkedList<PropertyDescriptor> properties;

	public DotLayoutSettings() {
		properties = new LinkedList<PropertyDescriptor>();
		properties.add(new PropertyDeclaration("Dot command", "getDotCommand", "setDotCommand", String.class));
		properties.add(new PropertyDeclaration("Temporary dot file path", "getTmpGraphFilePath", "setTmpGraphFilePath", String.class));
		properties.add(new PropertyDeclaration("Dot position scale factor", "getDotPositionScaleFactor", "setDotPositionScaleFactor", double.class));
	}
	public List<PropertyDescriptor> getPropertyDeclarations() {
		return properties;
	}

	public void loadPersistentProperties(Config config) {
		dotCommand = config.getString("DotLayout.dotCommand", "dot");
		tmpGraphFilePath = config.getString("DotLayout.tmpGraphFilePath", "tmp.dot");
		dotPositionScaleFactor = config.getDouble("DotLayout.dotPositionScaleFactor", 0.02);
	}

	public void storePersistentProperties(Config config) {
		config.set("DotLayout.dotCommand", dotCommand)	;
		config.set("DotLayout.tmpGraphFilePath", tmpGraphFilePath);
		config.set("DotLayout.dotPositionScaleFactor", Double.toString(dotPositionScaleFactor));
	}

	public static String getTmpGraphFilePath() {
		return DotLayoutSettings.tmpGraphFilePath;
	}

	public static void setTmpGraphFilePath(String tmpGraphFilePath) {
		DotLayoutSettings.tmpGraphFilePath = tmpGraphFilePath;
	}

	public static String getDotCommand() {
		return DotLayoutSettings.dotCommand;
	}

	public static void setDotCommand(String dotCommand) {
		DotLayoutSettings.dotCommand = dotCommand;
	}

	public static double getDotPositionScaleFactor() {
		return DotLayoutSettings.dotPositionScaleFactor;
	}

	public static void setDotPositionScaleFactor(double dotPositionScaleFactor) {
		DotLayoutSettings.dotPositionScaleFactor = dotPositionScaleFactor;
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
}
