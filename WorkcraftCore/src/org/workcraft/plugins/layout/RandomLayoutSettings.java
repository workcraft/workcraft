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

package org.workcraft.plugins.layout;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class RandomLayoutSettings implements SettingsPage {
	protected static double startX = 0;
	protected static double startY = 0;
	protected static double rangeX = 30;
	protected static double rangeY = 30;


	private static LinkedList<PropertyDescriptor> properties;

	public RandomLayoutSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<RandomLayoutSettings, Double>(
				this, "Start X", Double.class) {
			protected void setter(RandomLayoutSettings object, Double value) {
				RandomLayoutSettings.setStartX(value);
			}
			protected Double getter(RandomLayoutSettings object) {
				return RandomLayoutSettings.getStartX();
			}
		});

		properties.add(new PropertyDeclaration<RandomLayoutSettings, Double>(
				this, "Start Y", Double.class) {
			protected void setter(RandomLayoutSettings object, Double value) {
				RandomLayoutSettings.setStartY(value);
			}
			protected Double getter(RandomLayoutSettings object) {
				return RandomLayoutSettings.getStartY();
			}
		});

		properties.add(new PropertyDeclaration<RandomLayoutSettings, Double>(
				this, "Range X", Double.class) {
			protected void setter(RandomLayoutSettings object, Double value) {
				RandomLayoutSettings.setRangeX(value);
			}
			protected Double getter(RandomLayoutSettings object) {
				return RandomLayoutSettings.getRangeX();
			}
		});

		properties.add(new PropertyDeclaration<RandomLayoutSettings, Double>(
				this, "Range Y", Double.class) {
			protected void setter(RandomLayoutSettings object, Double value) {
				RandomLayoutSettings.setRangeY(value);
			}
			protected Double getter(RandomLayoutSettings object) {
				return RandomLayoutSettings.getRangeY();
			}
		});
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		startX = config.getDouble("RandomLayout.startX", 0);
		startY = config.getDouble("RandomLayout.startY", 0);
		rangeX = config.getDouble("RandomLayout.rangeX", 30);
		rangeY = config.getDouble("RandomLayout.rangeY", 30);
	}

	public void save(Config config) {
		config.setDouble("RandomLayout.startX", startX);
		config.setDouble("RandomLayout.startY", startY);
		config.setDouble("RandomLayout.rangeX", rangeX);
		config.setDouble("RandomLayout.rangeY", rangeY);
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
	@Override
	public String getName() {
		return "Random";
	}
}
