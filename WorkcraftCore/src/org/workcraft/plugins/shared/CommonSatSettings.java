package org.workcraft.plugins.shared;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CommonSatSettings implements Settings {

    public enum SatSolver {
        MINISAT("MiniSat"),
        CLASP("Clasp");

        public final String name;

        SatSolver(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonSatSettings";

    private static final String keySatSolver = prefix + ".satSolver";
    private static final String keyClaspCommand = prefix + ".claspCommand";
    private static final String keyMinisatCommand = prefix + ".minisatCommand";

    private static final SatSolver defaultSatSolver = SatSolver.CLASP;
    private static final String defaultClaspCommand = "clasp";
    private static final String defaultMinisatCommand = "minisat";

    private static SatSolver satSolver = defaultSatSolver;
    private static String claspCommand = defaultClaspCommand;
    private static String minisatCommand = defaultMinisatCommand;

    public CommonSatSettings() {
        properties.add(new PropertyDeclaration<CommonSatSettings, SatSolver>(
                this, "SAT solver", SatSolver.class, true, false, false) {
            protected void setter(CommonSatSettings object, SatSolver value) {
                setSatSolver(value);
            }
            protected SatSolver getter(CommonSatSettings object) {
                return getSatSolver();
            }
        });

        properties.add(new PropertyDeclaration<CommonSatSettings, String>(
                this, "Clasp solver command", String.class, true, false, false) {
            protected void setter(CommonSatSettings object, String value) {
                setClaspCommand(value);
            }
            protected String getter(CommonSatSettings object) {
                return getClaspCommand();
            }
        });

        properties.add(new PropertyDeclaration<CommonSatSettings, String>(
                this, "MiniSat solver command", String.class, true, false, false) {
            protected void setter(CommonSatSettings object, String value) {
                setMinisatCommand(value);
            }
            protected String getter(CommonSatSettings object) {
                return getMinisatCommand();
            }
        });
    }

    @Override
    public void load(Config config) {
        setSatSolver(config.getEnum(keySatSolver, SatSolver.class, defaultSatSolver));
        setClaspCommand(config.getString(keyClaspCommand, defaultClaspCommand));
        setMinisatCommand(config.getString(keyMinisatCommand, defaultMinisatCommand));
    }

    @Override
    public void save(Config config) {
        config.setEnum(keySatSolver, SatSolver.class, getSatSolver());
        config.set(keyClaspCommand, getClaspCommand());
        config.set(keyMinisatCommand, getMinisatCommand());
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "SAT solver";
    }

    public static SatSolver getSatSolver() {
        return satSolver;
    }

    public static void setSatSolver(SatSolver value) {
        satSolver = value;
    }

    public static String getClaspCommand() {
        return claspCommand;
    }

    public static void setClaspCommand(String value) {
        claspCommand = value;
    }

    public static String getMinisatCommand() {
        return minisatCommand;
    }

    public static void setMinisatCommand(String value) {
        minisatCommand = value;
    }

}
