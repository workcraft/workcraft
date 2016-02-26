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

package org.workcraft.plugins.punf;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PunfUtilitySettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.punf";

    private static final String keyCommand = prefix + ".command";
    private static final String keyExtraArgs = prefix + ".args";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyUsePnmlUnfolding = prefix + ".usePnmlUnfolding";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\UnfoldingTools\\punf.exe" : "tools/UnfoldingTools/punf";
    private static final String defaultExtraArgs = "-r";
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final Boolean defaultUsePnmlUnfolding = true;

    private static String command = defaultCommand;
    private static String extraArgs = defaultExtraArgs;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static Boolean usePnmlUnfolding = defaultUsePnmlUnfolding;

    public PunfUtilitySettings() {
        properties.add(new PropertyDeclaration<PunfUtilitySettings, String>(
                this, "Punf command", String.class, true, false, false) {
            protected void setter(PunfUtilitySettings object, String value) {
                setCommand(value);
            }
            protected String getter(PunfUtilitySettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<PunfUtilitySettings, String>(
                this, "Additional parameters", String.class, true, false, false) {
            protected void setter(PunfUtilitySettings object, String value) {
                setExtraArgs(value);
            }
            protected String getter(PunfUtilitySettings object) {
                return getExtraArgs();
            }
        });

        properties.add(new PropertyDeclaration<PunfUtilitySettings, Boolean>(
                this, "Output stdout", Boolean.class, true, false, false) {
            protected void setter(PunfUtilitySettings object, Boolean value) {
                setPrintStdout(value);
            }
            protected Boolean getter(PunfUtilitySettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<PunfUtilitySettings, Boolean>(
                this, "Output stderr", Boolean.class, true, false, false) {
            protected void setter(PunfUtilitySettings object, Boolean value) {
                setPrintStderr(value);
            }
            protected Boolean getter(PunfUtilitySettings object) {
                return getPrintStderr();
            }
        });

        properties.add(new PropertyDeclaration<PunfUtilitySettings, Boolean>(
                this, "Use PNML-based unfolding (where possible)", Boolean.class, true, false, false) {
            protected void setter(PunfUtilitySettings object, Boolean value) {
                setUsePnmlUnfolding(value);
            }
            protected Boolean getter(PunfUtilitySettings object) {
                return getUsePnmlUnfolding();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(keyCommand, defaultCommand));
        setExtraArgs(config.getString(keyExtraArgs, defaultExtraArgs));
        setUsePnmlUnfolding(config.getBoolean(keyUsePnmlUnfolding, defaultUsePnmlUnfolding));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.set(keyExtraArgs, getExtraArgs());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyUsePnmlUnfolding, getUsePnmlUnfolding());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "Punf";
    }

    public static String getCommand() {
        return command;
    }

    public static void setCommand(String value) {
        command = value;
    }

    public static String getExtraArgs() {
        return extraArgs;
    }

    public static void setExtraArgs(String value) {
        extraArgs = value;
    }

    public static Boolean getPrintStdout() {
        return printStdout;
    }

    public static void setPrintStdout(Boolean value) {
        printStdout = value;
    }

    public static Boolean getPrintStderr() {
        return printStderr;
    }

    public static void setPrintStderr(Boolean value) {
        printStderr = value;
    }

    public static Boolean getUsePnmlUnfolding() {
        return usePnmlUnfolding;
    }

    public static void setUsePnmlUnfolding(Boolean value) {
        usePnmlUnfolding = value;
    }

    public static String getUnfoldingExtension(boolean tryPnml) {
        return tryPnml && getUsePnmlUnfolding() ? ".pnml" : ".mci";
    }

    public static String getToolSuffix(boolean tryPnml) {
        return tryPnml && getUsePnmlUnfolding() ? "" : "-mci";
    }

}
