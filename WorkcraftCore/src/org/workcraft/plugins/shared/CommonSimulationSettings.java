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

public class CommonSimulationSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonSimulationSettings";

    private static final String keyUseEnabledForeground  = prefix + ".useEnabledForeground";
    private static final String keyEnabledForegroundColor  = prefix + ".enabledForegroundColor";
    private static final String keyUseEnabledBackground  = prefix + ".useEnabledBackground";
    private static final String keyEnabledBackgroundColor  = prefix + ".enabledBackgroundColor";

    private static final boolean deafultUseEnabledForeground  = true;
    private static final Color deafultEnabledForegroundColor = new Color(1.0f, 0.5f, 0.0f);
    private static final boolean deafultUseEnabledBackground  = false;
    private static final Color deafultEnabledBackgroundColor = new Color(1.0f, 0.5f, 0.0f);

    private static boolean useEnabledForeground = deafultUseEnabledForeground;
    private static Color enabledForegroundColor = deafultEnabledForegroundColor;
    private static boolean useEnabledBackground = deafultUseEnabledBackground;
    private static Color enabledBackgroundColor = deafultEnabledBackgroundColor;

    public CommonSimulationSettings() {
        properties.add(new PropertyDeclaration<CommonSimulationSettings, Boolean>(
                this, "Use enabled component foreground", Boolean.class, true, false, false) {
            protected void setter(CommonSimulationSettings object, Boolean value) {
                setUseEnabledForeground(value);
            }
            protected Boolean getter(CommonSimulationSettings object) {
                return getUseEnabledForeground();
            }
        });

        properties.add(new PropertyDeclaration<CommonSimulationSettings, Color>(
                this, "Enabled component foreground", Color.class, true, false, false) {
            protected void setter(CommonSimulationSettings object, Color value) {
                setEnabledForegroundColor(value);
            }
            protected Color getter(CommonSimulationSettings object) {
                return getEnabledForegroundColor();
            }
        });

        properties.add(new PropertyDeclaration<CommonSimulationSettings, Boolean>(
                this, "Use enabled component background", Boolean.class, true, false, false) {
            protected void setter(CommonSimulationSettings object, Boolean value) {
                setUseEnabledBackground(value);
            }
            protected Boolean getter(CommonSimulationSettings object) {
                return getUseEnabledBackground();
            }
        });

        properties.add(new PropertyDeclaration<CommonSimulationSettings, Color>(
                this, "Enabled component background", Color.class, true, false, false) {
            protected void setter(CommonSimulationSettings object, Color value) {
                setEnabledBackgroundColor(value);
            }
            protected Color getter(CommonSimulationSettings object) {
                return getEnabledBackgroundColor();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setUseEnabledForeground(config.getBoolean(keyUseEnabledForeground, deafultUseEnabledForeground));
        setEnabledForegroundColor(config.getColor(keyEnabledForegroundColor, deafultEnabledForegroundColor));
        setUseEnabledBackground(config.getBoolean(keyUseEnabledBackground, deafultUseEnabledBackground));
        setEnabledBackgroundColor(config.getColor(keyEnabledBackgroundColor, deafultEnabledBackgroundColor));
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyUseEnabledForeground, getUseEnabledForeground());
        config.setColor(keyEnabledForegroundColor, getEnabledForegroundColor());
        config.setBoolean(keyUseEnabledBackground, getUseEnabledBackground());
        config.setColor(keyEnabledBackgroundColor, getEnabledBackgroundColor());
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Simulation";
    }

    public static void setUseEnabledForeground(Boolean value) {
        useEnabledForeground = value;
    }

    public static Boolean getUseEnabledForeground() {
        return useEnabledForeground;
    }

    public static void setEnabledForegroundColor(Color value) {
        enabledForegroundColor = value;
    }

    public static Color getEnabledForegroundColor() {
        return useEnabledForeground ? enabledForegroundColor : null;
    }

    public static void setUseEnabledBackground(Boolean value) {
        useEnabledBackground = value;
    }

    public static Boolean getUseEnabledBackground() {
        return useEnabledBackground;
    }

    public static void setEnabledBackgroundColor(Color value) {
        enabledBackgroundColor = value;
    }

    public static Color getEnabledBackgroundColor() {
        return useEnabledBackground ? enabledBackgroundColor : null;
    }

}
