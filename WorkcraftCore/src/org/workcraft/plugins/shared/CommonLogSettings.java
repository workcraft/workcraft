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
import org.workcraft.gui.propertyeditor.Settings;

public class CommonLogSettings implements Settings {
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "CommonLogSettings";

	private static final String keyTextSize  = prefix + ".textSize";
	private static final String keyTextColor  = prefix + ".textColor";
	private static final String keyInfoBackground  = prefix + ".infoBackground";
	private static final String keyWarningBackground  = prefix + ".warningBackground";
	private static final String keyErrorBackground  = prefix + ".warningBackground";
	private static final String keyStdoutBackground  = prefix + ".stdoutBackground";
	private static final String keyStderrBackground  = prefix + ".stderrBackground";

	private static final int defaultTextSize = 12;
	private static final Color defaultTextColor = Color.BLACK;
	private static final Color defaultInfoBackground = new Color(0.8f, 1.0f, 0.8f);
	private static final Color defaultWarningBackground = new Color(1.0f, 0.8f, 0.0f);
	private static final Color defaultErrorBackground = new Color(1.0f, 0.7f, 0.7f);
	private static final Color defaultStdoutBackground = new Color(0.9f, 0.9f, 0.9f);
	private static final Color defaultStderrBackground = new Color(1.0f, 0.9f, 0.9f);

	private static int textSize = defaultTextSize;
	private static Color textColor = defaultTextColor;
	private static Color infoBackground = defaultInfoBackground;
	private static Color warningBackground = defaultWarningBackground;
	private static Color errorBackground = defaultErrorBackground;
	private static Color stdoutBackground = defaultStdoutBackground;
	private static Color stderrBackground = defaultStderrBackground;

	public CommonLogSettings() {
		properties.add(new PropertyDeclaration<CommonLogSettings, Integer>(
				this, "Text size", Integer.class, true, false, false) {
			protected void setter(CommonLogSettings object, Integer value) {
				setTextSize(value);
			}
			protected Integer getter(CommonLogSettings object) {
				return getTextSize();
			}
		});

		properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
				this, "Text color", Color.class, true, false, false) {
			protected void setter(CommonLogSettings object, Color value) {
				setTextColor(value);
			}
			protected Color getter(CommonLogSettings object) {
				return getTextColor();
			}
		});

		properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
				this, "Important info background", Color.class, true, false, false) {
			protected void setter(CommonLogSettings object, Color value) {
				setInfoBackground(value);
			}
			protected Color getter(CommonLogSettings object) {
				return getInfoBackground();
			}
		});

		properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
				this, "Warning background", Color.class, true, false, false) {
			protected void setter(CommonLogSettings object, Color value) {
				setWarningBackground(value);
			}
			protected Color getter(CommonLogSettings object) {
				return getWarningBackground();
			}
		});

		properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
				this, "Error background", Color.class, true, false, false) {
			protected void setter(CommonLogSettings object, Color value) {
				setErrorBackground(value);
			}
			protected Color getter(CommonLogSettings object) {
				return getErrorBackground();
			}
		});

		properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
				this, "Backend stdout background", Color.class, true, false, false) {
			protected void setter(CommonLogSettings object, Color value) {
				setStdoutBackground(value);
			}
			protected Color getter(CommonLogSettings object) {
				return getStdoutBackground();
			}
		});

		properties.add(new PropertyDeclaration<CommonLogSettings, Color>(
				this, "Backend stderr background", Color.class, true, false, false) {
			protected void setter(CommonLogSettings object, Color value) {
				setStderrBackground(value);
			}
			protected Color getter(CommonLogSettings object) {
				return getStderrBackground();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setTextSize(config.getInt(keyTextSize, defaultTextSize));
		setTextColor(config.getColor(keyTextColor, defaultTextColor));
		setInfoBackground(config.getColor(keyInfoBackground, defaultInfoBackground));
		setWarningBackground(config.getColor(keyWarningBackground, defaultWarningBackground));
		setErrorBackground(config.getColor(keyErrorBackground, defaultErrorBackground));
		setStdoutBackground(config.getColor(keyStdoutBackground, defaultStdoutBackground));
		setStderrBackground(config.getColor(keyStderrBackground, defaultStderrBackground));
	}

	@Override
	public void save(Config config) {
		config.setDouble(keyTextSize, getTextSize());
		config.setColor(keyTextColor, getTextColor());
		config.setColor(keyInfoBackground, getInfoBackground());
		config.setColor(keyWarningBackground, getWarningBackground());
		config.setColor(keyErrorBackground, getErrorBackground());
		config.setColor(keyStdoutBackground, getStdoutBackground());
		config.setColor(keyStderrBackground, getStderrBackground());
	}

	@Override
	public String getSection() {
		return "Common";
	}

	@Override
	public String getName() {
		return "Log";
	}

	public static int getTextSize() {
		return textSize;
	}

	public static void setTextSize(int value) {
		textSize = value;
	}

	public static Color getTextColor() {
		return textColor;
	}

	public static void setTextColor(Color value) {
		textColor = value;
	}

	public static Color getInfoBackground() {
		return infoBackground;
	}

	public static void setInfoBackground(Color value) {
		infoBackground = value;
	}

	public static Color getWarningBackground() {
		return warningBackground;
	}

	public static void setWarningBackground(Color value) {
		warningBackground = value;
	}

	public static Color getErrorBackground() {
		return errorBackground;
	}

	public static void setErrorBackground(Color value) {
		errorBackground = value;
	}

	public static Color getStdoutBackground() {
		return stdoutBackground;
	}

	public static void setStdoutBackground(Color value) {
		stdoutBackground = value;
	}

	public static Color getStderrBackground() {
		return stderrBackground;
	}

	public static void setStderrBackground(Color value) {
		stderrBackground = value;
	}

}
