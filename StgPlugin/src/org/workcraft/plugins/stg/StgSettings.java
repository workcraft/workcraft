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

import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class StgSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "StgSettings";

    private static final String keyDensityMapLevelLimit = prefix + ".densityMapLevelLimit";
    private static final String keyLowLevelSuffix = prefix + ".lowLevelSuffix";
    private static final String keyHighLevelSuffix = prefix + ".highLevelSuffix";
    private static final String keyGroupSignalConversion = prefix + ".groupSignalConversion";
    private static final String keyConceptsFolderLocation = prefix + ".conceptsFolderLocation";

    private static final Integer defaultDensityMapLevelLimit = 5;
    private static final String defaultLowLevelSuffix = "_LOW";
    private static final String defaultHighLevelSuffix = "_HIGH";
    private static final Boolean defaultGroupSignalConversion = false;
    private static final String defaultConceptsFolderLocation = DesktopApi.getOs().isWindows() ? "tools\\concepts\\" : "tools/concepts/";

    private static Integer densityMapLevelLimit = defaultDensityMapLevelLimit;
    private static String lowLevelSuffix = defaultLowLevelSuffix;
    private static String highLevelSuffix = defaultHighLevelSuffix;
    private static Boolean groupSignalConversion = defaultGroupSignalConversion;
    private static String conceptsFolderLocation = defaultConceptsFolderLocation;

    public StgSettings() {
        properties.add(new PropertyDeclaration<StgSettings, Integer>(
                this, "Maximum number of core density map levels", Integer.class, true, false, false) {
            protected void setter(StgSettings object, Integer value) {
                setDensityMapLevelLimit(value);
            }
            protected Integer getter(StgSettings object) {
                return getDensityMapLevelLimit();
            }
        });

        properties.add(new PropertyDeclaration<StgSettings, String>(
                this, "Signal low level suffix", String.class, true, false, false) {
            protected void setter(StgSettings object, String value) {
                if (value.length() < 2) {
                    signalLevelWarning();
                }
                if (checkSignalLevelSuffix(value)) {
                    signalLevelError();
                } else {
                    setLowLevelSuffix(value);
                }
            }
            protected String getter(StgSettings object) {
                return getLowLevelSuffix();
            }
        });

        properties.add(new PropertyDeclaration<StgSettings, String>(
                this, "Signal high level suffix", String.class, true, false, false) {
            protected void setter(StgSettings object, String value) {
                if (value.length() < 2) {
                    signalLevelWarning();
                }
                if (checkSignalLevelSuffix(value)) {
                    signalLevelError();
                } else {
                    setHighLevelSuffix(value);
                }
            }
            protected String getter(StgSettings object) {
                return getHighLevelSuffix();
            }
        });

        properties.add(new PropertyDeclaration<StgSettings, Boolean>(
                this, "Group signals on conversion", Boolean.class, true, false, false) {
            protected void setter(StgSettings object, Boolean value) {
                setGroupSignalConversion(value);
            }
            protected Boolean getter(StgSettings object) {
                return getGroupSignalConversion();
            }
        });

        properties.add(new PropertyDeclaration<StgSettings, String>(
                this, "Concepts folder location", String.class, true, false, false) {
            protected void setter(StgSettings object, String value) {
                setConceptsFolderLocation(value);
            }
            protected String getter(StgSettings object) {
                return getConceptsFolderLocation();
            }
        });
    }

    private boolean checkSignalLevelSuffix(String value) {
        boolean badValue = false;
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c) && (c != '_')) {
                badValue = true;
                break;
            }
        }
        return badValue;
    }

    private void signalLevelError() {
        JOptionPane.showMessageDialog(null,
                "Signal level suffix must only consist of letters, numbers and underscores.",
                "STG settings", JOptionPane.ERROR_MESSAGE);
    }

    private void signalLevelWarning() {
        JOptionPane.showMessageDialog(null,
                "Short signal level suffix increases the risk of name clashing.\n"
                        + "Consider making it at least two characters long.",
                        "STG settings", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setDensityMapLevelLimit(config.getInt(keyDensityMapLevelLimit, defaultDensityMapLevelLimit));
        setLowLevelSuffix(config.getString(keyLowLevelSuffix, defaultLowLevelSuffix));
        setHighLevelSuffix(config.getString(keyHighLevelSuffix, defaultHighLevelSuffix));
        setGroupSignalConversion(config.getBoolean(keyGroupSignalConversion, defaultGroupSignalConversion));
        setConceptsFolderLocation(config.getString(keyConceptsFolderLocation, defaultConceptsFolderLocation));
    }

    @Override
    public void save(Config config) {
        config.setInt(keyDensityMapLevelLimit, getDensityMapLevelLimit());
        config.set(keyLowLevelSuffix, getLowLevelSuffix());
        config.set(keyHighLevelSuffix, getHighLevelSuffix());
        config.setBoolean(keyGroupSignalConversion, getGroupSignalConversion());
        config.set(keyConceptsFolderLocation, getConceptsFolderLocation());
    }

    @Override
    public String getSection() {
        return "Models";
    }

    @Override
    public String getName() {
        return "Signal Transition Graph";
    }

    public static Integer getDensityMapLevelLimit() {
        return densityMapLevelLimit;
    }

    public static void setDensityMapLevelLimit(Integer value) {
        densityMapLevelLimit = value;
    }

    public static String getLowLevelSuffix() {
        return lowLevelSuffix;
    }

    public static void setLowLevelSuffix(String value) {
        if (value.length() > 0) {
            lowLevelSuffix = value;
        }
    }

    public static String getHighLevelSuffix() {
        return highLevelSuffix;
    }

    public static void setHighLevelSuffix(String value) {
        if (value.length() > 0) {
            highLevelSuffix = value;
        }
    }

    public static Boolean getGroupSignalConversion() {
        return groupSignalConversion;
    }

    public static void setGroupSignalConversion(Boolean value) {
        groupSignalConversion = value;
    }

    public static String getConceptsFolderLocation() {
        return conceptsFolderLocation;
    }

    public static void setConceptsFolderLocation(String value) {
        conceptsFolderLocation = value;
    }

}
