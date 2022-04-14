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

    private static Map<String, SubstitutionRule> importSubstitutionRules = null;
    private static String importSubstitutionRulesPath = null;
    private static Boolean importSubstitutionRulesInvert = null;

    private static Map<String, SubstitutionRule> exportSubstitutionRules = null;
    private static String exportSubstitutionRulesPath = null;
    private static Boolean exportSubstitutionRulesInvert = null;

    private LibraryManager() {
    }

    public static Library getLibrary() {
        String path = CircuitSettings.getGateLibrary();
        if ((path != null) && !path.equals(libraryPath)) {
            loadLibrary(path);
        }
        return library;
    }

    private static Library loadLibrary(String fileName) {
        if (fileName.isEmpty()) {
            LogUtils.logWarning("Gate library is not specified.");
            library = new Library();
            libraryPath = fileName;
        } else {
            File file = new File(fileName);
            if (FileUtils.checkAvailability(file, "Gate library access error", false)) {
                try {
                    InputStream genlibInputStream = new FileInputStream(file);
                    GenlibParser genlibParser = new GenlibParser(genlibInputStream);
                    if (DebugCommonSettings.getParserTracing()) {
                        genlibParser.enable_tracing();
                    } else {
                        genlibParser.disable_tracing();
                    }
                    LogUtils.logInfo("Reading gate library '" + file.getAbsolutePath() + "'.");
                    library = genlibParser.parseGenlib();
                    libraryPath = fileName;
                } catch (FileNotFoundException | org.workcraft.plugins.circuit.jj.genlib.ParseException e) {
                    LogUtils.logWarning("Could not parse the gate library '" + fileName + "'.");
                }
            }
        }
        return library;
    }

    public static Map<String, SubstitutionRule> getImportSubstitutionRules() {
        String path = CircuitSettings.getImportSubstitutionLibrary();
        boolean inversion = CircuitSettings.getInvertImportSubstitutionRules();
        if (!path.equals(importSubstitutionRulesPath) || (inversion != importSubstitutionRulesInvert)) {
            importSubstitutionRules = readSubstitutionRules(path, inversion);
            importSubstitutionRulesPath = path;
            importSubstitutionRulesInvert = inversion;
        }
        return importSubstitutionRules;
    }

    public static Map<String, SubstitutionRule> getExportSubstitutionRules() {
        String path = CircuitSettings.getExportSubstitutionLibrary();
        boolean invert = CircuitSettings.getInvertExportSubstitutionRules();
        if (!path.equals(exportSubstitutionRulesPath) || (invert != exportSubstitutionRulesInvert)) {
            exportSubstitutionRules = readSubstitutionRules(path, invert);
            exportSubstitutionRulesPath = path;
            exportSubstitutionRulesInvert = invert;
        }
        return exportSubstitutionRules;
    }

    private static HashMap<String, SubstitutionRule> readSubstitutionRules(String fileName, boolean invert) {
        HashMap<String, SubstitutionRule> result = new HashMap<>();
        if ((fileName != null) && !fileName.isEmpty()) {
            File libraryFile = new File(fileName);
            if (FileUtils.checkAvailability(libraryFile, "Access error for the file of substitutions", false)) {
                try {
                    InputStream substitutionInputStream = new FileInputStream(fileName);
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
                            + fileName + "'.");

                } catch (FileNotFoundException | ParseException e) {
                    LogUtils.logWarning("Could not parse the file of substitutions '" + fileName + "'.");
                }
            }
        }
        return result;
    }

}
