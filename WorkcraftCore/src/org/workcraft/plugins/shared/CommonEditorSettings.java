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

package org.workcraft.plugins.shared;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class CommonEditorSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	private static final String prefix = "CommonEditorSettings";
	private static final String backgroundColorKey = prefix + ".backgroundColor";
	private static final String showGridKey = prefix + ".showGrid";
	private static final String showRulersKey = prefix + ".showRulers";
	private static final String iconSizeKey = prefix + ".iconSize";
	private static final String debugClipboardKey = prefix + ".debugClipboard";

	private static final Color defaultBackgroundColor = Color.WHITE;
	private static final boolean defaultShowGrid = true;
	private static final boolean defaultShowRulers = true;
	private static final int defaultIconSize = 24;
	private static final boolean defaultDebugClipboard = false;

	private static Color backgroundColor = defaultBackgroundColor;
	private static boolean showGrid = defaultShowGrid;
	private static boolean showRulers = defaultShowRulers;
	private static int iconSize = defaultIconSize;
	private static boolean debugClipboard = defaultDebugClipboard;

	public String getSection() {
		return "Common";
	}

	public String getName() {
		return "Editor";
	}

	public CommonEditorSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<CommonEditorSettings, Color>(
				this, "Background color", Color.class) {
			protected void setter(CommonEditorSettings object, Color value) {
				CommonEditorSettings.setBackgroundColor(value);
			}
			protected Color getter(CommonEditorSettings object) {
				return CommonEditorSettings.getBackgroundColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
				this, "Show grid", Boolean.class) {
			protected void setter(CommonEditorSettings object, Boolean value) {
				CommonEditorSettings.setShowGrid(value);
			}
			protected Boolean getter(CommonEditorSettings object) {
				return CommonEditorSettings.getShowGrid();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
				this, "Show rulers", Boolean.class) {
			protected void setter(CommonEditorSettings object, Boolean value) {
				CommonEditorSettings.setShowRulers(value);
			}
			protected Boolean getter(CommonEditorSettings object) {
				return CommonEditorSettings.getShowRulers();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Integer>(
				this, "Icon width (pixels, 8-256)", Integer.class) {
			protected void setter(CommonEditorSettings object, Integer value) {
				CommonEditorSettings.setIconSize(value);
			}
			protected Integer getter(CommonEditorSettings object) {
				return CommonEditorSettings.getIconSize();
			}
		});

		properties.add(new PropertyDeclaration<CommonEditorSettings, Boolean>(
				this, "Debug clipboard", Boolean.class) {
			protected void setter(CommonEditorSettings object, Boolean value) {
				CommonEditorSettings.setDebugClipboard(value);
			}
			protected Boolean getter(CommonEditorSettings object) {
				return CommonEditorSettings.getDebugClipboard();
			}
		});

	}

	public static Boolean getDebugClipboard() {
		return CommonEditorSettings.debugClipboard;
	}

	public static void setDebugClipboard(Boolean value) {
		CommonEditorSettings.debugClipboard = value;
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		setBackgroundColor(config.getColor(backgroundColorKey, defaultBackgroundColor));
		setShowGrid(config.getBoolean(showGridKey, defaultShowGrid));
		setShowRulers(config.getBoolean(showRulersKey, defaultShowRulers));
		setIconSize(config.getInt(iconSizeKey, defaultIconSize));
		setDebugClipboard(config.getBoolean(debugClipboardKey, defaultDebugClipboard));
	}

	public void save(Config config) {
		config.setColor(backgroundColorKey, getBackgroundColor());
		config.setBoolean(showGridKey, getShowGrid());
		config.setBoolean(showRulersKey, getShowRulers());
		config.setInt(iconSizeKey, getIconSize());
		config.setBoolean(debugClipboardKey, getDebugClipboard());
	}

	public static Color getBackgroundColor() {
		return backgroundColor;
	}

	public static void setBackgroundColor(Color value) {
		backgroundColor = value;
	}

	public static void setShowGrid(Boolean value) {
		showGrid = value;
	}

	public static Boolean getShowGrid() {
		return showGrid;
	}

	public static void setShowRulers(Boolean value) {
		showRulers = value;
	}

	public static Boolean getShowRulers() {
		return showRulers;
	}

	public static int getIconSize() {
		return iconSize;
	}

	public static void setIconSize(int value) {
		if (value < 8) {
			value = 8;
		}
		if (value > 256) {
			value = 256;
		}
		iconSize = value;
	}

}
