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

package org.workcraft.plugins.mpsat;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;

public class MpsatUtilitySettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.mpsatVerification";

    private static final String keyCommand = prefix + ".command";
    private static final String keySolutionMode = prefix + ".solutionMode";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyDebugReach = prefix + ".debugReach";
    private static final String keyDebugCores = prefix + ".debugCores";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\UnfoldingTools\\mpsat.exe" : "tools/UnfoldingTools/mpsat";
    private static final SolutionMode defaultSolutionMode = SolutionMode.MINIMUM_COST;
    private static final String defaultArgs = "";
    private static final Boolean defaultAdvancedMode = false;
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final Boolean defaultDebugReach = false;
    private static final Boolean defaultDebugCores = false;

    private static String command = defaultCommand;
    private static SolutionMode solutionMode = defaultSolutionMode;
    private static String args = defaultArgs;
    private static Boolean advancedMode = defaultAdvancedMode;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static Boolean debugReach = defaultDebugReach;
    private static Boolean debugCores = defaultDebugCores;

    public MpsatUtilitySettings() {
        properties.add(new PropertyDeclaration<MpsatUtilitySettings, String>(
                this, "MPSat command for verification", String.class, true, false, false) {
            protected void setter(MpsatUtilitySettings object, String value) {
                setCommand(value);
            }
            protected String getter(MpsatUtilitySettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<MpsatUtilitySettings, SolutionMode>(
                this, "Solution mode", SolutionMode.class, true, false, false) {
            protected void setter(MpsatUtilitySettings object, SolutionMode value) {
                setSolutionMode(value);
            }
            protected SolutionMode getter(MpsatUtilitySettings object) {
                return getSolutionMode();
            }
        });

        properties.add(new PropertyDeclaration<MpsatUtilitySettings, String>(
                this, "Additional parameters", String.class, true, false, false) {
            protected void setter(MpsatUtilitySettings object, String value) {
                setArgs(value);
            }
            protected String getter(MpsatUtilitySettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<MpsatUtilitySettings, Boolean>(
                this, "Edit additional parameters before every call", Boolean.class, true, false, false) {
            protected void setter(MpsatUtilitySettings object, Boolean value) {
                setAdvancedMode(value);
            }
            protected Boolean getter(MpsatUtilitySettings object) {
                return getAdvancedMode();
            }
        });

        properties.add(new PropertyDeclaration<MpsatUtilitySettings, Boolean>(
                this, "Output stdout", Boolean.class, true, false, false) {
            protected void setter(MpsatUtilitySettings object, Boolean value) {
                setPrintStdout(value);
            }
            protected Boolean getter(MpsatUtilitySettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<MpsatUtilitySettings, Boolean>(
                this, "Output stderr", Boolean.class, true, false, false) {
            protected void setter(MpsatUtilitySettings object, Boolean value) {
                setPrintStderr(value);
            }
            protected Boolean getter(MpsatUtilitySettings object) {
                return getPrintStderr();
            }
        });

        properties.add(new PropertyDeclaration<MpsatUtilitySettings, Boolean>(
                this, "Output Reach expressions", Boolean.class, true, false, false) {
            protected void setter(MpsatUtilitySettings object, Boolean value) {
                setDebugReach(value);
            }
            protected Boolean getter(MpsatUtilitySettings object) {
                return getDebugReach();
            }
        });

        properties.add(new PropertyDeclaration<MpsatUtilitySettings, Boolean>(
                this, "Output conflict cores", Boolean.class, true, false, false) {
            protected void setter(MpsatUtilitySettings object, Boolean value) {
                setDebugCores(value);
            }
            protected Boolean getter(MpsatUtilitySettings object) {
                return getDebugCores();
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
        setSolutionMode(config.getEnum(keySolutionMode, SolutionMode.class, defaultSolutionMode));
        setArgs(config.getString(keyArgs, defaultArgs));
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setDebugReach(config.getBoolean(keyDebugReach, defaultDebugReach));
        setDebugCores(config.getBoolean(keyDebugCores, defaultDebugCores));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.setEnum(keySolutionMode, SolutionMode.class, getSolutionMode());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyAdvancedMode, getAdvancedMode());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyDebugReach, getDebugReach());
        config.setBoolean(keyDebugCores, getDebugCores());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "MPSat verification";
    }

    public static String getCommand() {
        return command;
    }

    public static void setCommand(String value) {
        command = value;
    }

    public static String getArgs() {
        return args;
    }

    public static void setArgs(String value) {
        args = value;
    }

    public static void setSolutionMode(SolutionMode value) {
        solutionMode = value;
    }

    public static SolutionMode getSolutionMode() {
        return solutionMode;
    }

    public static int getSolutionCount() {
        return (solutionMode == SolutionMode.ALL) ? 10 : 1;
    }

    public static Boolean getAdvancedMode() {
        return advancedMode;
    }

    public static void setAdvancedMode(Boolean value) {
        advancedMode = value;
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

    public static Boolean getDebugReach() {
        return debugReach;
    }

    public static void setDebugReach(Boolean value) {
        debugReach = value;
    }

    public static Boolean getDebugCores() {
        return debugCores;
    }

    public static void setDebugCores(Boolean value) {
        debugCores = value;
    }

}
