package org.workcraft.plugins.cpog;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractModelSettings;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DesktopApi;

import java.util.Collection;
import java.util.LinkedList;

public class CpogSettings extends AbstractModelSettings {

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
    private static final String prefix = "CpogSettings";

    private static final String keyScencoCommand = prefix + ".scencoCommand";
    private static final String keyEspressoCommand = prefix + ".espressoCommand";
    private static final String keyAbcTool = prefix + ".abcTool";
    private static final String keyPGMinerCommand = prefix + ".PGMinerCommand";
    private static final String keySatSolver = prefix + ".satSolver";
    private static final String keyClaspCommand = prefix + ".claspCommand";
    private static final String keyMinisatCommand = prefix + ".minisatCommand";

    private static final String defaultScencoCommand = BackendUtils.getToolPath("ScEnco", "scenco");
    private static final String defaultEspressoCommand = BackendUtils.getToolPath("Espresso", "espresso");
    private static final String defaultAbcTool = BackendUtils.getToolPath("Abc", "abc");
    private static final String defaultPgminerCommand = BackendUtils.getToolPath("PGMiner", "pgminer");
    private static final SatSolver defaultSatSolver = SatSolver.CLASP;
    private static final String defaultClaspCommand = DesktopApi.getOs().isWindows() ? "tools\\clasp\\clasp.exe" : "clasp";
    private static final String defaultMinisatCommand = DesktopApi.getOs().isWindows() ? "tools\\minisat\\minisat.exe" : "minisat";

    private static String scencoCommand = defaultScencoCommand;
    private static String espressoCommand = defaultEspressoCommand;
    private static String abcTool = defaultAbcTool;
    private static String pgminerCommand = defaultPgminerCommand;
    private static SatSolver satSolver = defaultSatSolver;
    private static String claspCommand = defaultClaspCommand;
    private static String minisatCommand = defaultMinisatCommand;

    static {
        properties.add(new PropertyDeclaration<>(String.class,
                "Scenco command",
                CpogSettings::setScencoCommand,
                CpogSettings::getScencoCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "Espresso command",
                CpogSettings::setEspressoCommand,
                CpogSettings::getEspressoCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "Abc command",
                CpogSettings::setAbcTool,
                CpogSettings::getAbcTool));

        properties.add(new PropertyDeclaration<>(String.class,
                "PG miner command",
                CpogSettings::setPgminerCommand,
                CpogSettings::getPgminerCommand));

        properties.add(new PropertyDeclaration<>(SatSolver.class,
                "SAT solver",
                CpogSettings::setSatSolver,
                CpogSettings::getSatSolver));

        properties.add(new PropertyDeclaration<>(String.class,
                "Clasp solver command",
                CpogSettings::setClaspCommand,
                CpogSettings::getClaspCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "MiniSat solver command",
                CpogSettings::setMinisatCommand,
                CpogSettings::getMinisatCommand));
    }

    @Override
    public void load(Config config) {
        setScencoCommand(config.getString(keyScencoCommand, defaultScencoCommand));
        setEspressoCommand(config.getString(keyEspressoCommand, defaultEspressoCommand));
        setAbcTool(config.getString(keyAbcTool, defaultAbcTool));
        setPgminerCommand(config.getString(keyPGMinerCommand, defaultPgminerCommand));
        setSatSolver(config.getEnum(keySatSolver, SatSolver.class, defaultSatSolver));
        setClaspCommand(config.getString(keyClaspCommand, defaultClaspCommand));
        setMinisatCommand(config.getString(keyMinisatCommand, defaultMinisatCommand));
    }

    @Override
    public void save(Config config) {
        config.set(keyScencoCommand, getScencoCommand());
        config.set(keyEspressoCommand, getEspressoCommand());
        config.set(keyAbcTool, getAbcTool());
        config.set(keyPGMinerCommand, getPgminerCommand());
        config.setEnum(keySatSolver, getSatSolver());
        config.set(keyClaspCommand, getClaspCommand());
        config.set(keyMinisatCommand, getMinisatCommand());
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public String getName() {
        return "Conditional Partial Order Graph";
    }

    public static String getScencoCommand() {
        return scencoCommand;
    }

    public static void setScencoCommand(String value) {
        scencoCommand = value;
    }

    public static String getEspressoCommand() {
        return espressoCommand;
    }

    public static void setEspressoCommand(String value) {
        espressoCommand = value;
    }

    public static String getAbcTool() {
        return abcTool;
    }

    public static void setAbcTool(String value) {
        abcTool = value;
    }

    public static void setPgminerCommand(String value) {
        pgminerCommand = value;
    }

    public static String getPgminerCommand() {
        return pgminerCommand;
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
