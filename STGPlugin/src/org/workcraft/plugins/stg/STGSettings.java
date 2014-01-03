/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.stg;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class STGSettings implements SettingsPage {
	private final static String keySettings = "StgSettings";
	private final static String keyInputColor = "inputColor";
	private final static String keyOutputColor = "outputColor";
	private final static String keyInternalColor = "internalColor";
	private final static String keyDummyColor = "dummyColor";

	private final static Color defaultInputColor = Color.RED.darker();
	private final static Color defaultOutputColor = Color.BLUE.darker();
	private final static Color defaultInternalColor = Color.GREEN.darker();
	private final static Color defaultDummyColor = Color.BLACK.darker();

	private static LinkedList<PropertyDescriptor> properties;
	private static Color inputColor = defaultInputColor;
	private static Color outputColor = defaultOutputColor;
	private static Color internalColor = defaultInternalColor;
	private static Color dummyColor = defaultDummyColor;

	public STGSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<STGSettings, Color>(
				this, "Input transition color", Color.class) {
			protected void setter(STGSettings object, Color value) {
				STGSettings.setInputColor(value);
			}
			protected Color getter(STGSettings object) {
				return STGSettings.getInputColor();
			}
		});

		properties.add(new PropertyDeclaration<STGSettings, Color>(
				this, "Output transition color", Color.class) {
			protected void setter(STGSettings object, Color value) {
				STGSettings.setOutputColor(value);
			}
			protected Color getter(STGSettings object) {
				return STGSettings.getOutputColor();
			}
		});

		properties.add(new PropertyDeclaration<STGSettings, Color>(
				this, "Internal transition color", Color.class) {
			protected void setter(STGSettings object, Color value) {
				STGSettings.setInternalColor(value);
			}
			protected Color getter(STGSettings object) {
				return STGSettings.getInternalColor();
			}
		});

		properties.add(new PropertyDeclaration<STGSettings, Color>(
				this, "Dummy transition color", Color.class) {
			protected void setter(STGSettings object, Color value) {
				STGSettings.setDummyColor(value);
			}
			protected Color getter(STGSettings object) {
				return STGSettings.getDummyColor();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setInputColor(config.getColor(keySettings + "." + keyInputColor, defaultInputColor));
		setOutputColor(config.getColor(keySettings + "." + keyOutputColor, defaultOutputColor));
		setInternalColor(config.getColor(keySettings + "." + keyInternalColor, defaultInternalColor));
		setDummyColor(config.getColor(keySettings + "." + keyDummyColor, defaultDummyColor));
	}

	@Override
	public void save(Config config) {
		config.setColor(keySettings + "." + keyInputColor, getInputColor());
		config.setColor(keySettings + "." + keyOutputColor, getOutputColor());
		config.setColor(keySettings + "." + keyInternalColor, getInternalColor());
		config.setColor(keySettings + "." + keyDummyColor, getDummyColor());
	}

	@Override
	public String getSection() {
		return "Models";
	}

	@Override
	public String getName() {
		return "Signal Transition Graph";
	}

	public static void setInputColor(Color value) {
		STGSettings.inputColor = value;
	}

	public static Color getInputColor() {
		return inputColor;
	}

	public static void setOutputColor(Color value) {
		STGSettings.outputColor = value;
	}

	public static Color getOutputColor() {
		return outputColor;
	}

	public static void setInternalColor(Color value) {
		STGSettings.internalColor = value;
	}

	public static Color getInternalColor() {
		return internalColor;
	}

	public static void setDummyColor(Color value) {
		STGSettings.dummyColor = value;
	}

	public static Color getDummyColor() {
		return dummyColor;
	}

}
