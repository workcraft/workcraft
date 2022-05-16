package org.workcraft;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Options {

    private static final String FLAG_PREFIX = "-";
    private static final String DIR_OPTION = "-dir:";
    private static final String CONFIG_OPTION = "-config:";
    private static final String CONFIG_ADD_OPTION = "-config-add:";
    private static final String EXEC_OPTION = "-exec:";
    private static final String PORT_OPTION = "-port:";
    private static final String NOGUI_OPTION = "-nogui";
    private static final String NOCONFIG_OPTION = "-noconfig";
    private static final String NOCONFIG_LOAD_OPTION = "-noconfig-load";
    private static final String NOCONFIG_SAVE_OPTION = "-noconfig-save";
    private static final String VERSION_OPTION = "-version";
    private static final String HELP_OPTION = "-help";

    private static final String CONFIG_ENV = "WORKCRAFT_CONFIG";
    private static final String CONFIG_ADD_ENV = "WORKCRAFT_CONFIG_ADD";

    private static final String HELP = "Usage:  workcraft [OPTIONS] [FILES]"
            + "\nOPTIONS - space-separated list of the following options:"
            + "\n  " + DIR_OPTION + "DIR           path to the working directory"
            + "\n  " + CONFIG_OPTION + "CONFIG     user config file (default is global config.xml)"
            + "\n  " + CONFIG_ADD_OPTION + "CONFIG additional read-only config file to override user config settings"
            + "\n  " + EXEC_OPTION + "SCRIPT       JavaScript file or one-liner to execute on startup"
            + "\n  " + PORT_OPTION + "PORT         reuse running instance on PORT to open FILES"
            + "\n  " + NOGUI_OPTION + "             run in console mode"
            + "\n  " + NOCONFIG_LOAD_OPTION + "     use default settings instead of loading them from user config"
            + "\n  " + NOCONFIG_SAVE_OPTION + "     do not overwriting user config on exit"
            + "\n  " + NOCONFIG_OPTION + "          use default settings and do not overwriting user config"
            + "\n  " + VERSION_OPTION + "           report the version information and exit"
            + "\n  " + HELP_OPTION + "              display this help message and exit"
            + "\nFILES - space-separated list of work files to open or arguments for SCRIPT"
            + "\n"
            + "\nNote that file path parameters CONFIG, SCRIPT and FILES are relative to the working directory."
            + "\n"
            + "\nUse environment variables " + CONFIG_ENV + " and " + CONFIG_ADD_ENV + " as lower priority"
            + "\nalternatives to command line options " + CONFIG_OPTION + " and " + CONFIG_ADD_OPTION + " respectively."
            + "\n";

    private final Collection<String> paths;
    private final File directory;
    private final String config;
    private final String configAddition;
    private final String script;
    private final Integer port;
    private final boolean noGuiFlag;
    private final boolean noConfigLoadFlag;
    private final boolean noConfigSaveFlag;
    private final boolean helpFlag;
    private final boolean versionFlag;

    Options(String[] args) {
        this(Arrays.asList(args));
    }

    Options(List<String> args) {
        paths = args.stream()
                .filter(arg -> (arg != null) && !arg.isEmpty() && !arg.startsWith(FLAG_PREFIX))
                .collect(Collectors.toList());

        directory = getOptionLastValue(args, DIR_OPTION, File::new);
        config = getOptionOrEnvLastValue(args, CONFIG_OPTION, CONFIG_ENV);
        configAddition = getOptionOrEnvLastValue(args, CONFIG_ADD_OPTION, CONFIG_ADD_ENV);
        script = getOptionLastValue(args, EXEC_OPTION);
        port = getOptionLastValue(args, PORT_OPTION, Integer::valueOf);
        noGuiFlag = args.contains(NOGUI_OPTION);
        noConfigLoadFlag = args.contains(NOCONFIG_LOAD_OPTION) || args.contains(NOCONFIG_OPTION);
        noConfigSaveFlag = args.contains(NOCONFIG_SAVE_OPTION) || args.contains(NOCONFIG_OPTION);
        helpFlag = args.contains(HELP_OPTION);
        versionFlag = args.contains(VERSION_OPTION);
    }

    private String getOptionOrEnvLastValue(List<String> args, String optionPrefix, String envName) {
        String optionLastValue = getOptionLastValue(args, optionPrefix);
        return optionLastValue == null ? System.getenv(envName) : optionLastValue;
    }

    private <T> T getOptionLastValue(List<String> args, String optionPrefix, Function<String, T> transformer) {
        String optionLastValue = getOptionLastValue(args, optionPrefix);
        return optionLastValue == null ? null : transformer.apply(optionLastValue);
    }

    private String getOptionLastValue(List<String> args, String optionPrefix) {
        return args.stream()
                .filter(arg -> (arg != null) && arg.startsWith(optionPrefix))
                .map(arg -> arg.substring(optionPrefix.length()))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public Collection<String> getPaths() {
        return Collections.unmodifiableCollection(paths);
    }

    public File getDirectory() {
        return directory;
    }

    public String getConfig() {
        return config;
    }

    public String getConfigAddition() {
        return configAddition;
    }

    public String getScript() {
        return script;
    }

    public Integer getPort() {
        return port;
    }

    public boolean hasNoGuiFlag() {
        return noGuiFlag;
    }

    public boolean hasNoConfigLoadFlag() {
        return noConfigLoadFlag;
    }

    public boolean hasNoConfigSaveFlag() {
        return noConfigSaveFlag;
    }

    public boolean hasHelpFlag() {
        return helpFlag;
    }

    public boolean hasVersionFlag() {
        return versionFlag;
    }

    public static String getHelpMessage() {
        return HELP;
    }

}
