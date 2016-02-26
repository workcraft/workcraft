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
import org.workcraft.gui.propertyeditor.Settings;

public class STGSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "StgSettings";

    private static final String keyInputColor = prefix + ".inputColor";
    private static final String keyOutputColor = prefix + ".outputColor";
    private static final String keyInternalColor = prefix + ".internalColor";
    private static final String keyDummyColor = prefix + ".dummyColor";
    private static final String keyShowToggle = prefix + ".showToggle";
    private static final String keyDensityMapLevelLimit = prefix + ".densityMapLevelLimit";

    private static final Color defaultInputColor = Color.RED.darker();
    private static final Color defaultOutputColor = Color.BLUE.darker();
    private static final Color defaultInternalColor = Color.GREEN.darker();
    private static final Color defaultDummyColor = Color.BLACK.darker();
    private static final boolean defaultShowToggle = false;
    private static final Integer defaultDensityMapLevelLimit = 5;

    private static Color inputColor = defaultInputColor;
    private static Color outputColor = defaultOutputColor;
    private static Color internalColor = defaultInternalColor;
    private static Color dummyColor = defaultDummyColor;
    private static boolean showToggle = defaultShowToggle;
    private static Integer densityMapLevelLimit = defaultDensityMapLevelLimit;

    public STGSettings() {
        properties.add(new PropertyDeclaration<STGSettings, Color>(
                this, "Input transition color", Color.class, true, false, false) {
            protected void setter(STGSettings object, Color value) {
                setInputColor(value);
            }
            protected Color getter(STGSettings object) {
                return getInputColor();
            }
        });

        properties.add(new PropertyDeclaration<STGSettings, Color>(
                this, "Output transition color", Color.class, true, false, false) {
            protected void setter(STGSettings object, Color value) {
                setOutputColor(value);
            }
            protected Color getter(STGSettings object) {
                return getOutputColor();
            }
        });

        properties.add(new PropertyDeclaration<STGSettings, Color>(
                this, "Internal transition color", Color.class, true, false, false) {
            protected void setter(STGSettings object, Color value) {
                setInternalColor(value);
            }
            protected Color getter(STGSettings object) {
                return getInternalColor();
            }
        });

        properties.add(new PropertyDeclaration<STGSettings, Color>(
                this, "Dummy transition color", Color.class, true, false, false) {
            protected void setter(STGSettings object, Color value) {
                setDummyColor(value);
            }
            protected Color getter(STGSettings object) {
                return getDummyColor();
            }
        });

        properties.add(new PropertyDeclaration<STGSettings, Boolean>(
                this, "Show signal toggle (~)", Boolean.class, true, false, false) {
            protected void setter(STGSettings object, Boolean value) {
                setShowToggle(value);
            }
            protected Boolean getter(STGSettings object) {
                return getShowToggle();
            }
        });

        properties.add(new PropertyDeclaration<STGSettings, Integer>(
                this, "Maximum number of core density map levels", Integer.class, true, false, false) {
            protected void setter(STGSettings object, Integer value) {
                setDensityMapLevelLimit(value);
            }
            protected Integer getter(STGSettings object) {
                return getDensityMapLevelLimit();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setInputColor(config.getColor(keyInputColor, defaultInputColor));
        setOutputColor(config.getColor(keyOutputColor, defaultOutputColor));
        setInternalColor(config.getColor(keyInternalColor, defaultInternalColor));
        setDummyColor(config.getColor(keyDummyColor, defaultDummyColor));
        setShowToggle(config.getBoolean(keyShowToggle, defaultShowToggle));
        setDensityMapLevelLimit(config.getInt(keyDensityMapLevelLimit, defaultDensityMapLevelLimit));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyInputColor, getInputColor());
        config.setColor(keyOutputColor, getOutputColor());
        config.setColor(keyInternalColor, getInternalColor());
        config.setColor(keyDummyColor, getDummyColor());
        config.setBoolean(keyShowToggle, getShowToggle());
        config.setInt(keyDensityMapLevelLimit, getDensityMapLevelLimit());
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
        inputColor = value;
    }

    public static Color getInputColor() {
        return inputColor;
    }

    public static void setOutputColor(Color value) {
        outputColor = value;
    }

    public static Color getOutputColor() {
        return outputColor;
    }

    public static void setInternalColor(Color value) {
        internalColor = value;
    }

    public static Color getInternalColor() {
        return internalColor;
    }

    public static void setDummyColor(Color value) {
        dummyColor = value;
    }

    public static Color getDummyColor() {
        return dummyColor;
    }

    public static Boolean getShowToggle() {
        return showToggle;
    }

    public static void setShowToggle(Boolean value) {
        showToggle = value;
    }

    public static Integer getDensityMapLevelLimit() {
        return densityMapLevelLimit;
    }

    public static void setDensityMapLevelLimit(Integer value) {
        densityMapLevelLimit = value;
    }

}
