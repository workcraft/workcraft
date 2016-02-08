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

package org.workcraft.plugins.petrify;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PetrifyExtraUtilitySettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
    private static final String prefix = "Tools";

    private static final String keyDrawAstgCommand = prefix + ".draw_astg.command";
    private static final String keyDrawAstgArgs = prefix + ".draw_astg.args";
    private static final String keyWriteSgCommand = prefix + ".write_sg.command";
    private static final String keyWriteSgArgs = prefix + ".write_sg.args";

    private static final String defaultDrawAstgCommand = (DesktopApi.getOs().isWindows() ? "tools\\PetrifyTools\\draw_astg.exe" : "tools/PetrifyTools/draw_astg");
    private static final String defaultDrawAstgArgs = "";
    private static final String defaultWriteSgCommand = (DesktopApi.getOs().isWindows() ? "tools\\PetrifyTools\\write_sg.exe" : "tools/PetrifyTools/write_sg");
    private static final String defaultWriteSgArgs = "";

    private static String drawAstgCommand = defaultDrawAstgCommand;
    private static String drawAstgArgs = defaultDrawAstgArgs;
    private static String writeSgCommand = defaultWriteSgCommand;
    private static String writeSgArgs = defaultWriteSgArgs;

    public PetrifyExtraUtilitySettings() {
        properties.add(new PropertyDeclaration<PetrifyExtraUtilitySettings, String>(
                this, "WriteSG command", String.class, true, false, false) {
            protected void setter(PetrifyExtraUtilitySettings object, String value) {
                setWriteSgCommand(value);
            }
            protected String getter(PetrifyExtraUtilitySettings object) {
                return getWriteSgCommand();
            }
        });

        properties.add(new PropertyDeclaration<PetrifyExtraUtilitySettings, String>(
                this, "Additional parameters for WriteSG", String.class, true, false, false) {
            protected void setter(PetrifyExtraUtilitySettings object, String value) {
                setWriteSgArgs(value);
            }
            protected String getter(PetrifyExtraUtilitySettings object) {
                return getWriteSgArgs();
            }
        });

        properties.add(new PropertyDeclaration<PetrifyExtraUtilitySettings, String>(
                this, "DrawASTG command", String.class, true, false, false) {
            protected void setter(PetrifyExtraUtilitySettings object, String value) {
                setDrawAstgCommand(value);
            }
            protected String getter(PetrifyExtraUtilitySettings object) {
                return getDrawAstgCommand();
            }
        });

        properties.add(new PropertyDeclaration<PetrifyExtraUtilitySettings, String>(
                this, "Additional parameters for DrawASTG", String.class, true, false, false) {
            protected void setter(PetrifyExtraUtilitySettings object, String value) {
                setDrawAstgArgs(value);
            }
            protected String getter(PetrifyExtraUtilitySettings object) {
                return getDrawAstgArgs();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setDrawAstgCommand(config.getString(keyDrawAstgCommand, defaultDrawAstgCommand));
        setDrawAstgArgs(config.getString(keyDrawAstgArgs, defaultDrawAstgArgs));
        setWriteSgCommand(config.getString(keyWriteSgCommand, defaultWriteSgCommand));
        setWriteSgArgs(config.getString(keyWriteSgArgs, defaultWriteSgArgs));
    }

    @Override
    public void save(Config config) {
        config.set(keyDrawAstgCommand, getDrawAstgCommand());
        config.set(keyDrawAstgArgs, getDrawAstgArgs());
        config.set(keyWriteSgCommand, getWriteSgCommand());
        config.set(keyWriteSgArgs, getWriteSgArgs());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "Petrify extras";
    }

    public static String getDrawAstgCommand() {
        return drawAstgCommand;
    }

    public static void setDrawAstgCommand(String value) {
        drawAstgCommand = value;
    }

    public static String getDrawAstgArgs() {
        return drawAstgArgs;
    }

    public static void setDrawAstgArgs(String value) {
        drawAstgArgs = value;
    }

    public static String getWriteSgCommand() {
        return writeSgCommand;
    }

    public static void setWriteSgCommand(String value) {
        writeSgCommand = value;
    }

    public static String getWriteSgArgs() {
        return writeSgArgs;
    }

    public static void setWriteSgArgs(String value) {
        writeSgArgs = value;
    }

}
