package org.workcraft.plugins.circuit.utils;

import org.workcraft.plugins.builtin.settings.AnalysisDecorationSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.TextUtils;

public final class StatsUtils {

    private StatsUtils() {
    }

    public static String getHtmlStatsHeader(String text, Integer problemCount, Integer undefinedCount,
            Integer fixerCount, Integer clearCount) {

        String nbsp = "&nbsp;";

        String problemText = problemCount == null ? ""
                : nbsp + TextUtils.getHtmlSpanHighlight(nbsp + problemCount + nbsp,
                AnalysisDecorationSettings.getProblemColor());

        String undefinedText = undefinedCount == null ? ""
                : nbsp + TextUtils.getHtmlSpanHighlight(nbsp + undefinedCount + nbsp,
                VisualCommonSettings.getFillColor());

        String fixerText = fixerCount == null ? ""
                : nbsp + TextUtils.getHtmlSpanHighlight(nbsp + fixerCount + nbsp,
                AnalysisDecorationSettings.getFixerColor());

        String clearText = clearCount == null ? ""
                : nbsp + TextUtils.getHtmlSpanHighlight(nbsp + clearCount + nbsp,
                AnalysisDecorationSettings.getClearColor());

        return "<html>" + text + ":" + problemText + undefinedText + fixerText + clearText + "</html>";
    }

}
