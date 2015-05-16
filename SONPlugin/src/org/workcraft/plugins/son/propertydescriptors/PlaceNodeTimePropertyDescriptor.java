package org.workcraft.plugins.son.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.plugins.son.elements.PlaceNode;

public class PlaceNodeTimePropertyDescriptor implements PropertyDescriptor{
	private final PlaceNode c;

	public PlaceNodeTimePropertyDescriptor(PlaceNode c) {
		this.c = c;
	}

	@Override
	public String getName() {
		return "Duration";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

	@Override
	public boolean isTemplatable() {
		return false;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return c.getDuration();
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		String input = (String)value;

		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();

		if(!TimeAlg.isValidInput(input)){
			input = c.getDuration();
			JOptionPane.showMessageDialog(mainWindow,
					"Input value is not a valid duration interval.\n"
							+ "The format must be xxxx-xxxx, where x is positive integer.",
					"Cannot change property", JOptionPane.WARNING_MESSAGE);
		}

		c.setDuration((String)value);
	}

	@Override
	public Map<? extends Object, String> getChoice() {
		return null;
	}

}
