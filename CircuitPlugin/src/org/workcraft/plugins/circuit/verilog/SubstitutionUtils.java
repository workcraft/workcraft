package org.workcraft.plugins.circuit.verilog;

import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.jj.substitution.ParseException;
import org.workcraft.plugins.circuit.jj.substitution.SubstitutionParser;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class SubstitutionUtils {

    public static HashMap<String, SubstitutionRule> readExportSubsritutionRules() {
        return readSubsritutionRules(
                CircuitSettings.getExportSubstitutionLibrary(),
                CircuitSettings.getInvertExportSubstitutionRules());
    }

    public static HashMap<String, SubstitutionRule> readImportSubsritutionRules() {
        return readSubsritutionRules(
                CircuitSettings.getImportSubstitutionLibrary(),
                CircuitSettings.getInvertImportSubstitutionRules());
    }

    private static HashMap<String, SubstitutionRule> readSubsritutionRules(String fileName, boolean invert) {
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

                } catch (FileNotFoundException e) {
                } catch (ParseException e) {
                    LogUtils.logWarning("Could not parse the file of substitutions '" + fileName + "'.");
                }
            }
        }
        return result;
    }

    public static String getModuleSubstitutionName(String moduleName,
            SubstitutionRule substitutionRule, String msg) {

        if (substitutionRule != null) {
            String newModuleName = substitutionRule.newName;
            if (newModuleName != null) {
                if (msg == null) {
                    msg = "";
                }
                LogUtils.logInfo(msg + "Renaming module '" + moduleName + "' to '" + newModuleName + "'.");
                moduleName = newModuleName;
            }
        }
        return moduleName;
    }

    public static String getContactSubstitutionName(String contactName,
            SubstitutionRule substitutionRule, String msg) {

        if (substitutionRule != null) {
            String newContactName = substitutionRule.substitutions.get(contactName);
            if (newContactName != null) {
                if (msg == null) {
                    msg = "";
                }
                LogUtils.logInfo(msg + "Renaming contact '" + contactName + "' to '" + newContactName + "'.");
                contactName = newContactName;
            }
        }
        return contactName;
    }

}
