package org.workcraft.plugins.circuit.verilog;

import org.workcraft.utils.LogUtils;

public final class SubstitutionUtils {

    private SubstitutionUtils() {
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
                if (msg != null) {
                    LogUtils.logInfo(msg + "Renaming contact '" + contactName + "' to '" + newContactName + "'.");
                }
                contactName = newContactName;
            }
        }
        return contactName;
    }

}
