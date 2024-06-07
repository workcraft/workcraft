package org.workcraft.plugins.circuit.genlib;

import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.jj.genlib.GenlibParser;
import org.workcraft.plugins.circuit.jj.substitution.ParseException;
import org.workcraft.plugins.circuit.jj.substitution.SubstitutionParser;
import org.workcraft.plugins.circuit.verilog.SubstitutionRule;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LibraryManager {

    private static Library library;
    private static String libraryPath = null;
    private static long libraryModtime = 0;

    private static Map<String, SubstitutionRule> importSubstitutionRules = null;
    private static String importSubstitutionRulesPath = null;
    private static long importSubstitutionRulesModtime = 0;
    private static Boolean importSubstitutionRulesInvert = null;

    private static Map<String, SubstitutionRule> exportSubstitutionRules = null;
    private static String exportSubstitutionRulesPath = null;
    private static long exportSubstitutionRulesModtime = 0;
    private static Boolean exportSubstitutionRulesInvert = null;

    private LibraryManager() {
    }

    public static Library getLibrary() {
        String path = CircuitSettings.getGateLibrary();
        long modtime = FileUtils.getModtimeOrZero(path);
        if (!path.equals(libraryPath) || (modtime != libraryModtime)) {
            library = loadLibrary(path);
            libraryPath = path;
            libraryModtime = modtime;
        }
        return library;
    }

    private static Library loadLibrary(String path) {
        if (path.isEmpty()) {
            LogUtils.logWarning("Gate library is not specified.");
            return new Library();
        }
        File file = new File(path);
        if (FileUtils.checkFileReadability(file, "Gate library access error")) {
            try {
                InputStream genlibInputStream = new FileInputStream(file);
                GenlibParser genlibParser = new GenlibParser(genlibInputStream);
                if (DebugCommonSettings.getParserTracing()) {
                    genlibParser.enable_tracing();
                } else {
                    genlibParser.disable_tracing();
                }
                LogUtils.logInfo("Reading gate library '" + file.getAbsolutePath() + "'.");
                return genlibParser.parseGenlib();
            } catch (FileNotFoundException | org.workcraft.plugins.circuit.jj.genlib.ParseException e) {
                LogUtils.logWarning("Could not parse the gate library '" + path + "'.");
            }
        }
        return null;
    }

    public static Map<String, SubstitutionRule> getImportSubstitutionRules() {
        String path = CircuitSettings.getImportSubstitutionLibrary();
        long modtime = FileUtils.getModtimeOrZero(path);
        boolean inversion = CircuitSettings.getInvertImportSubstitutionRules();
        if (!path.equals(importSubstitutionRulesPath)
                || (modtime != importSubstitutionRulesModtime)
                || (inversion != importSubstitutionRulesInvert)) {

            importSubstitutionRules = readSubstitutionRules(path, inversion);
            importSubstitutionRulesPath = path;
            importSubstitutionRulesModtime = modtime;
            importSubstitutionRulesInvert = inversion;
        }
        return importSubstitutionRules;
    }

    public static Map<String, SubstitutionRule> getExportSubstitutionRules() {
        String path = CircuitSettings.getExportSubstitutionLibrary();
        long modtime = FileUtils.getModtimeOrZero(path);
        boolean invert = CircuitSettings.getInvertExportSubstitutionRules();
        if (!path.equals(exportSubstitutionRulesPath)
                || (modtime != exportSubstitutionRulesModtime)
                || (invert != exportSubstitutionRulesInvert)) {

            exportSubstitutionRules = readSubstitutionRules(path, invert);
            exportSubstitutionRulesPath = path;
            exportSubstitutionRulesModtime = modtime;
            exportSubstitutionRulesInvert = invert;
        }
        return exportSubstitutionRules;
    }

    private static HashMap<String, SubstitutionRule> readSubstitutionRules(String path, boolean invert) {
        HashMap<String, SubstitutionRule> result = new HashMap<>();
        if ((path != null) && !path.isEmpty()) {
            File libraryFile = new File(path);
            if (FileUtils.checkFileReadability(libraryFile, "Access error for the file of substitutions")) {
                try {
                    InputStream substitutionInputStream = new FileInputStream(path);
                    SubstitutionParser substitutionParser = new SubstitutionParser(substitutionInputStream, invert);
                    if (DebugCommonSettings.getParserTracing()) {
                        substitutionParser.enable_tracing();
                    } else {
                        substitutionParser.disable_tracing();
                    }
                    List<SubstitutionRule> rules = substitutionParser.parseSubstitutionRules();
                    for (SubstitutionRule rule: rules) {
                        result.put(rule.oldName, rule);
                    }
                    LogUtils.logInfo("Renaming gates and pins using "
                            + (invert ? "inverted" : "direct")
                            + " rules in the file of substitutions '"
                            + path + "'.");

                } catch (FileNotFoundException | ParseException e) {
                    LogUtils.logWarning("Could not parse the file of substitutions '" + path + "'.");
                }
            }
        }
        return result;
    }

}
