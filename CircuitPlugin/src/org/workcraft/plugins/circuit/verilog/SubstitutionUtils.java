package org.workcraft.plugins.circuit.verilog;

import org.workcraft.plugins.builtin.settings.CommonDebugSettings;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.Contact;
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

    public static HashMap<String, SubstitutionRule> readSubsritutionRules() {
        HashMap<String, SubstitutionRule> result = new HashMap<>();
        String substitutionsFileName = CircuitSettings.getSubstitutionLibrary();
        if ((substitutionsFileName != null) && !substitutionsFileName.isEmpty()) {
            File libraryFile = new File(substitutionsFileName);
            if (FileUtils.checkAvailability(libraryFile, "Access error for the file of substitutions", false)) {
                try {
                    InputStream substitutionInputStream = new FileInputStream(substitutionsFileName);
                    boolean invertSubstitutionRules = CircuitSettings.getInvertSubstitutionRules();
                    SubstitutionParser substitutionParser = new SubstitutionParser(substitutionInputStream, invertSubstitutionRules);
                    if (CommonDebugSettings.getParserTracing()) {
                        substitutionParser.enable_tracing();
                    } else {
                        substitutionParser.disable_tracing();
                    }
                    List<SubstitutionRule> rules = substitutionParser.parseSubstitutionRules();
                    for (SubstitutionRule rule: rules) {
                        result.put(rule.oldName, rule);
                    }
                    LogUtils.logInfo("Renaming gates and pins using "
                            + (invertSubstitutionRules ? "inverted" : "direct")
                            + " rules in the file of substitutions '"
                            + substitutionsFileName + "'.");

                } catch (FileNotFoundException e) {
                } catch (ParseException e) {
                    LogUtils.logWarning("Could not parse the file of substitutions '" + substitutionsFileName + "'.");
                }
            }
        }
        return result;
    }

    public static String getContactSubstitutionName(Contact contact, SubstitutionRule substitutionRule, String instanceFlatName) {
        String contactName = contact.getName();
        if (substitutionRule != null) {
            String newContactName = substitutionRule.substitutions.get(contactName);
            if (newContactName != null) {
                LogUtils.logInfo("In component '" + instanceFlatName + "' renaming contact '" + contactName + "' to '" + newContactName + "'.");
                contactName = newContactName;
            }
        }
        return contactName;
    }

}
