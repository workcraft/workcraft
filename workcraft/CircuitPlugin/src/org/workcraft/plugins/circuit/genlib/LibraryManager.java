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
    private static File libraryFile = null;
    private static long libraryModtime = 0;

    private static Map<String, SubstitutionRule> importSubstitutionRules = null;
    private static File importSubstitutionRulesFile = null;
    private static long importSubstitutionRulesModtime = 0;
    private static Boolean importSubstitutionRulesInvert = null;

    private static Map<String, SubstitutionRule> exportSubstitutionRules = null;
    private static File exportSubstitutionRulesFile = null;
    private static long exportSubstitutionRulesModtime = 0;
    private static Boolean exportSubstitutionRulesInvert = null;

    private LibraryManager() {
    }

    public static Library getLibrary() {
        File file = FileUtils.getEvalPathFile(CircuitSettings.getGateLibrary());
        long modtime = FileUtils.getModtimeOrZero(file);
        if ((libraryFile == null) || !libraryFile.equals(file) || (modtime != libraryModtime)) {
            library = loadLibrary(file);
            libraryFile = file;
            libraryModtime = modtime;
        }
        return library;
    }

    private static Library loadLibrary(File file) {
        if (file == null) {
            LogUtils.logWarning("Gate library is not specified.");
            return new Library();
        }
        if (FileUtils.checkFileReadability(file, "Gate library access error")) {
            try {
                InputStream genlibInputStream = new FileInputStream(file);
                GenlibParser genlibParser = new GenlibParser(genlibInputStream);
                if (DebugCommonSettings.getParserTracing()) {
                    genlibParser.enable_tracing();
                } else {
                    genlibParser.disable_tracing();
                }
                LogUtils.logInfo("Reading gate library '" + file.getAbsolutePath() + "'");
                return genlibParser.parseGenlib();
            } catch (FileNotFoundException | org.workcraft.plugins.circuit.jj.genlib.ParseException e) {
                LogUtils.logWarning("Could not parse gate library '" + file.getPath() + "'\n" + e.getMessage());
            }
        }
        return null;
    }

    public static Map<String, SubstitutionRule> getImportSubstitutionRules() {
        File file = FileUtils.getEvalPathFile(CircuitSettings.getImportSubstitutionLibrary());
        long modtime = FileUtils.getModtimeOrZero(file);
        boolean inversion = CircuitSettings.getInvertImportSubstitutionRules();
        if ((importSubstitutionRulesFile == null)
                || !importSubstitutionRulesFile.equals(file)
                || (modtime != importSubstitutionRulesModtime)
                || (inversion != importSubstitutionRulesInvert)) {

            importSubstitutionRules = readSubstitutionRules(file, inversion);
            importSubstitutionRulesFile = file;
            importSubstitutionRulesModtime = modtime;
            importSubstitutionRulesInvert = inversion;
        }
        return importSubstitutionRules;
    }

    public static Map<String, SubstitutionRule> getExportSubstitutionRules() {
        File file = FileUtils.getEvalPathFile(CircuitSettings.getExportSubstitutionLibrary());
        long modtime = FileUtils.getModtimeOrZero(file);
        boolean invert = CircuitSettings.getInvertExportSubstitutionRules();
        if ((exportSubstitutionRulesFile == null)
                || !exportSubstitutionRulesFile.equals(file)
                || (modtime != exportSubstitutionRulesModtime)
                || (invert != exportSubstitutionRulesInvert)) {

            exportSubstitutionRules = readSubstitutionRules(file, invert);
            exportSubstitutionRulesFile = file;
            exportSubstitutionRulesModtime = modtime;
            exportSubstitutionRulesInvert = invert;
        }
        return exportSubstitutionRules;
    }

    private static HashMap<String, SubstitutionRule> readSubstitutionRules(File file, boolean invert) {
        HashMap<String, SubstitutionRule> result = new HashMap<>();
        if ((file != null) && FileUtils.checkFileReadability(file, "Access error for the file of substitutions")) {
            try {
                InputStream substitutionInputStream = new FileInputStream(file);
                SubstitutionParser substitutionParser = new SubstitutionParser(substitutionInputStream, invert);
                if (DebugCommonSettings.getParserTracing()) {
                    substitutionParser.enable_tracing();
                } else {
                    substitutionParser.disable_tracing();
                }
                List<SubstitutionRule> rules = substitutionParser.parseSubstitutionRules();
                for (SubstitutionRule rule : rules) {
                    result.put(rule.oldName, rule);
                }
                LogUtils.logInfo("Renaming gates and pins using "
                        + (invert ? "inverted" : "direct")
                        + " rules in the file of substitutions '"
                        + file.getPath() + "'");

            } catch (FileNotFoundException | ParseException e) {
                LogUtils.logWarning("Could not parse the file of substitutions '" + file.getPath() + "'\n"
                        + e.getMessage());
            }
        }
        return result;
    }

}
