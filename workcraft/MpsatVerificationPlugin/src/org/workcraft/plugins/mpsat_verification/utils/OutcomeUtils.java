package org.workcraft.plugins.mpsat_verification.utils;

import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;

public class OutcomeUtils {

    public static final String TITLE = "Verification results";

    public static void showOutcome(boolean propertyHolds, String message, boolean interactive) {
        if (interactive) {
            showOutcome(propertyHolds, message);
        } else {
            logOutcome(propertyHolds, message);
        }
    }

    public static void showOutcome(boolean propertyHolds, String message) {
        if (propertyHolds) {
            DialogUtils.showInfo(message, TITLE);
        } else {
            DialogUtils.showWarning(message, TITLE);
        }
    }

    public static void logOutcome(boolean propertyHolds, String message) {
        if (propertyHolds) {
            LogUtils.logInfo(message);
        } else {
            LogUtils.logWarning(message);
        }
    }

}
