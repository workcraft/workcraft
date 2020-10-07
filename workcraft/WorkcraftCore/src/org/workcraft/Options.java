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
    private static final String EXEC_OPTION = "-exec:";
    private static final String PORT_OPTION = "-port:";
    private static final String NOGUI_OPTION = "-nogui";
    private static final String NOCONFIG_OPTION = "-noconfig";
    private static final String VERSION_OPTION = "-version";
    private static final String HELP_OPTION = "-help";

    private static final String HELP = "Usage:  workcraft [OPTIONS] [FILES]\n" +
            "OPTIONS - space-separated list of the following options:\n" +
            "  " + DIR_OPTION + "<PATH>     pass working directory\n" +
            "  " + EXEC_OPTION + "<SCRIPT>  JavaScript file or one-liner to execute on startup\n" +
            "  " + PORT_OPTION + "<PORT>    reuse running instance on PORT to open FILES\n" +
            "  " + NOGUI_OPTION + "          run in console mode\n" +
            "  " + NOCONFIG_OPTION + "       use default settings without overwriting user config\n" +
            "  " + VERSION_OPTION + "        report the version information and exit\n" +
            "  " + HELP_OPTION + "           display this help message and exit\n" +
            "FILES - space-separated list of work files to open (or arguments for script)\n";

    private final Collection<String> paths;
    private final File directory;
    private final String script;
    private final Integer port;
    private final boolean noGuiFlag;
    private final boolean noConfigFlag;
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
        script = getOptionLastValue(args, EXEC_OPTION);
        port = getOptionLastValue(args, PORT_OPTION, Integer::valueOf);
        noGuiFlag = args.contains(NOGUI_OPTION);
        noConfigFlag = args.contains(NOCONFIG_OPTION);
        helpFlag = args.contains(HELP_OPTION);
        versionFlag = args.contains(VERSION_OPTION);
    }

    private String getOptionLastValue(List<String> args, String prefix) {
        return getOptionLastValue(args, prefix, s -> s);
    }

    private <T> T getOptionLastValue(List<String> args, String prefix, Function<String, T> transformer) {
        return args.stream()
                .filter(arg -> (arg != null) && arg.startsWith(prefix))
                .map(arg -> arg.substring(prefix.length()))
                .map(transformer)
                .reduce((first, second) -> second)
                .orElse(null);
    }


    public Collection<String> getPaths() {
        return Collections.unmodifiableCollection(paths);
    }

    public File getDirectory() {
        return directory;
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

    public boolean hasNoConfigFlag() {
        return noConfigFlag;
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
