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
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.SettingsPage;

public class CommonVisualSettings implements SettingsPage {
	private static LinkedList<PropertyDescriptor> properties;

	protected static double baseSize = 1.0;
	protected static double strokeWidth = 0.1;
	protected static Color borderColor = Color.BLACK;
	protected static Color fillColor = Color.WHITE;
	private static boolean labelVisibility = true;
	private static Positioning labelPositioning = Positioning.TOP;
	private static Color labelColor = Color.BLACK;
	private static boolean nameVisibility = false;
	private static Positioning namePositioning = Positioning.BOTTOM;
	private static Color nameColor = Color.BLUE.darker();
	private static boolean useEnabledForegroundColor = true;
	private static Color enabledForegroundColor = new Color(1.0f, 0.5f, 0.0f);
	private static boolean useEnabledBackgroundColor = false;
	private static Color enabledBackgroundColor = new Color(0.0f, 0.0f, 0.0f);

	public String getSection() {
		return "Common";
	}

	public String getName() {
		return "Visual";
	}

	public CommonVisualSettings() {
		properties = new LinkedList<PropertyDescriptor>();

		properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
				this, "Base size (cm)", Double.class) {
			protected void setter(CommonVisualSettings object, Double value) {
				CommonVisualSettings.setBaseSize(value);
			}
			protected Double getter(CommonVisualSettings object) {
				return CommonVisualSettings.getBaseSize();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Double>(
				this, "Stroke width (cm)", Double.class) {
			protected void setter(CommonVisualSettings object, Double value) {
				CommonVisualSettings.setStrokeWidth(value);
			}
			protected Double getter(CommonVisualSettings object) {
				return CommonVisualSettings.getStrokeWidth();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
				this, "Border color", Color.class) {
			protected void setter(CommonVisualSettings object, Color value) {
				CommonVisualSettings.setBorderColor(value);
			}
			protected Color getter(CommonVisualSettings object) {
				return CommonVisualSettings.getBorderColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
				this, "Fill color", Color.class) {
			protected void setter(CommonVisualSettings object, Color value) {
				CommonVisualSettings.setFillColor(value);
			}
			protected Color getter(CommonVisualSettings object) {
				return CommonVisualSettings.getFillColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Boolean>(
				this, "Show component labels", Boolean.class) {
			protected void setter(CommonVisualSettings object, Boolean value) {
				CommonVisualSettings.setLabelVisibility(value);
			}
			protected Boolean getter(CommonVisualSettings object) {
				return CommonVisualSettings.getLabelVisibility();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Positioning>(
				this, "Label positioning", Positioning.class, Positioning.getChoice()) {
			protected void setter(CommonVisualSettings object, Positioning value) {
				CommonVisualSettings.setLabelPositioning(value);
			}
			protected Positioning getter(CommonVisualSettings object) {
				return CommonVisualSettings.getLabelPositioning();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
				this, "Label color", Color.class) {
			protected void setter(CommonVisualSettings object, Color value) {
				CommonVisualSettings.setLabelColor(value);
			}
			protected Color getter(CommonVisualSettings object) {
				return CommonVisualSettings.getLabelColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Boolean>(
				this, "Show component names", Boolean.class) {
			protected void setter(CommonVisualSettings object, Boolean value) {
				CommonVisualSettings.setNameVisibility(value);
			}
			protected Boolean getter(CommonVisualSettings object) {
				return CommonVisualSettings.getNameVisibility();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Positioning>(
				this, "Name positioning", Positioning.class, Positioning.getChoice()) {
			protected void setter(CommonVisualSettings object, Positioning value) {
				CommonVisualSettings.setNamePositioning(value);
			}
			protected Positioning getter(CommonVisualSettings object) {
				return CommonVisualSettings.getNamePositioning();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
				this, "Name color", Color.class) {
			protected void setter(CommonVisualSettings object, Color value) {
				CommonVisualSettings.setNameColor(value);
			}
			protected Color getter(CommonVisualSettings object) {
				return CommonVisualSettings.getNameColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Boolean>(
				this, "Use enabled component foreground", Boolean.class) {
			protected void setter(CommonVisualSettings object, Boolean value) {
				CommonVisualSettings.setUseEnabledForegroundColor(value);
			}
			protected Boolean getter(CommonVisualSettings object) {
				return CommonVisualSettings.getUseEnabledForegroundColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
				this, "Enabled component foreground", Color.class) {
			protected void setter(CommonVisualSettings object, Color value) {
				CommonVisualSettings.setEnabledForegroundColor(value);
			}
			protected Color getter(CommonVisualSettings object) {
				return CommonVisualSettings.getEnabledForegroundColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Boolean>(
				this, "Use enabled component background", Boolean.class) {
			protected void setter(CommonVisualSettings object, Boolean value) {
				CommonVisualSettings.setUseEnabledBackgroundColor(value);
			}
			protected Boolean getter(CommonVisualSettings object) {
				return CommonVisualSettings.getUseEnabledBackgroundColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonVisualSettings, Color>(
				this, "Enabled component background", Color.class) {
			protected void setter(CommonVisualSettings object, Color value) {
				CommonVisualSettings.setEnabledBackgroundColor(value);
			}
			protected Color getter(CommonVisualSettings object) {
				return CommonVisualSettings.getEnabledBackgroundColor();
			}
		});
	}

	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	public void load(Config config) {
		baseSize = config.getDouble("CommonVisualSettings.baseSize", 1.0);
		strokeWidth = config.getDouble("CommonVisualSettings.strokeWidth", 0.1);
		borderColor = config.getColor("CommonVisualSettings.foregroundColor", Color.BLACK);
		fillColor = config.getColor("CommonVisualSettings.fillColor", Color.WHITE);

		labelVisibility = config.getBoolean("CommonVisualSettings.labelVisibility", true);
		labelPositioning = config.getTextPositioning("CommonVisualSettings.labelPositioning", Positioning.TOP);
		labelColor = config.getColor("CommonVisualSettings.labelColor", Color.BLACK);

		nameVisibility = config.getBoolean("CommonVisualSettings.nameVisibility", true);
		namePositioning = config.getTextPositioning("CommonVisualSettings.namePositioning", Positioning.BOTTOM);
		nameColor = config.getColor("CommonVisualSettings.nameColor", Color.BLUE.darker());

		useEnabledForegroundColor = config.getBoolean("CommonVisualSettings.useEnabledForegroundColor", true);
		enabledForegroundColor = config.getColor("CommonVisualSettings.enabledForegroundColor", new Color(1.0f, 0.5f, 0.0f));

		useEnabledBackgroundColor = config.getBoolean("CommonVisualSettings.useEnabledBackgroundColor", false);
		enabledBackgroundColor = config.getColor("CommonVisualSettings.enabledBackgroundColor", new Color(1.0f, 0.5f, 0.0f));
	}

	public void save(Config config) {
		config.setDouble("CommonVisualSettings.baseSize", baseSize);
		config.setDouble("CommonVisualSettings.strokeWidth", strokeWidth);
		config.setColor("CommonVisualSettings.borderColor", borderColor);
		config.setColor("CommonVisualSettings.fillColor", fillColor);

		config.setBoolean("CommonVisualSettings.labelVisibility", labelVisibility);
		config.setColor("CommonVisualSettings.labelColor", labelColor);
		config.setTextPositioning("CommonVisualSettings.labelPositioning", labelPositioning);

		config.setBoolean("CommonVisualSettings.nameVisibility", nameVisibility);
		config.setColor("CommonVisualSettings.nameColor", nameColor);
		config.setTextPositioning("CommonVisualSettings.namePositioning", namePositioning);

		config.setBoolean("CommonVisualSettings.useEnabledForegroundColor", useEnabledForegroundColor);
		config.setColor("CommonVisualSettings.enabledBackgroundColor", enabledBackgroundColor);

		config.setBoolean("CommonVisualSettings.useEnabledBackgroundColor", useEnabledBackgroundColor);
		config.setColor("CommonVisualSettings.enabledForegroundColor", enabledForegroundColor);
	}

	public static double getBaseSize() {
		return baseSize;
	}

	public static void setBaseSize(double value) {
		CommonVisualSettings.baseSize = value;
	}

	public static double getStrokeWidth() {
		return strokeWidth;
	}

	public static void setStrokeWidth(double value) {
		CommonVisualSettings.strokeWidth = value;
	}

	public static Color getBorderColor() {
		return borderColor;
	}

	public static void setBorderColor(Color value) {
		CommonVisualSettings.borderColor = value;
	}

	public static Color getFillColor() {
		return fillColor;
	}

	public static void setFillColor(Color value) {
		CommonVisualSettings.fillColor = value;
	}

	public static Boolean getLabelVisibility() {
		return labelVisibility;
	}

	public static void setLabelVisibility(Boolean value) {
		CommonVisualSettings.labelVisibility = value;
	}

	public static Positioning getLabelPositioning() {
		return labelPositioning;
	}

	public static void setLabelPositioning(Positioning value) {
		CommonVisualSettings.labelPositioning = value;
	}

	public static Color getLabelColor() {
		return labelColor;
	}

	public static void setLabelColor(Color value) {
		CommonVisualSettings.labelColor = value;
	}

	public static Boolean getNameVisibility() {
		return nameVisibility;
	}

	public static void setNameVisibility(Boolean value) {
		CommonVisualSettings.nameVisibility = value;
	}

	public static Positioning getNamePositioning() {
		return namePositioning;
	}

	public static void setNamePositioning(Positioning value) {
		CommonVisualSettings.namePositioning = value;
	}

	public static Color getNameColor() {
		return nameColor;
	}

	public static void setNameColor(Color value) {
		CommonVisualSettings.nameColor = value;
	}

	public static void setUseEnabledForegroundColor(Boolean value) {
		CommonVisualSettings.useEnabledForegroundColor = value;
	}

	public static Boolean getUseEnabledForegroundColor() {
		return useEnabledForegroundColor;
	}

	public static void setEnabledForegroundColor(Color value) {
		CommonVisualSettings.enabledForegroundColor = value;
	}

	public static Color getEnabledForegroundColor() {
		return useEnabledForegroundColor ? enabledForegroundColor : null;
	}

	public static void setUseEnabledBackgroundColor(Boolean value) {
		CommonVisualSettings.useEnabledBackgroundColor = value;
	}

	public static Boolean getUseEnabledBackgroundColor() {
		return useEnabledBackgroundColor;
	}

	public static void setEnabledBackgroundColor(Color value) {
		CommonVisualSettings.enabledBackgroundColor = value;
	}

	public static Color getEnabledBackgroundColor() {
		return useEnabledBackgroundColor ? enabledBackgroundColor : null;
	}

}
