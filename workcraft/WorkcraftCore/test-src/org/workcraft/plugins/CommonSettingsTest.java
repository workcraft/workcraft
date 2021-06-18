package org.workcraft.plugins;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.builtin.settings.*;

class CommonSettingsTest {

    @BeforeAll
    static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    void commonCommentSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonCommentSettings";

        Assertions.assertEquals(Config.toString(CommentCommonSettings.getBaseSize()),
                framework.getConfigVar(prefix + ".baseSize", false));

        Assertions.assertEquals(Config.toString(CommentCommonSettings.getStrokeWidth()),
                framework.getConfigVar(prefix + ".strokeWidth", false));

        Assertions.assertEquals(Config.toString(CommentCommonSettings.getTextAlignment()),
                framework.getConfigVar(prefix + ".textAlignment", false));

        Assertions.assertEquals(Config.toString(CommentCommonSettings.getTextColor()),
                framework.getConfigVar(prefix + ".textColor", false));

        Assertions.assertEquals(Config.toString(CommentCommonSettings.getBorderColor()),
                framework.getConfigVar(prefix + ".borderColor", false));

        Assertions.assertEquals(Config.toString(CommentCommonSettings.getFillColor()),
                framework.getConfigVar(prefix + ".fillColor", false));

        Assertions.assertEquals(Config.toString(CommentCommonSettings.getFontSize()),
                framework.getConfigVar(prefix + ".fontSize", false));
    }

    @Test
    void commonDebugSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonDebugSettings";

        Assertions.assertEquals(Config.toString(DebugCommonSettings.getVerboseImport()),
                framework.getConfigVar(prefix + ".verboseImport", false));

        Assertions.assertEquals(Config.toString(DebugCommonSettings.getParserTracing()),
                framework.getConfigVar(prefix + ".parserTracing", false));

        Assertions.assertEquals(Config.toString(DebugCommonSettings.getVerboseCompatibilityManager()),
                framework.getConfigVar(prefix + ".verboseCompatibilityManager", false));

        Assertions.assertEquals(Config.toString(DebugCommonSettings.getShortExportHeader()),
                framework.getConfigVar(prefix + ".shortExportHeader", false));
    }

    @Test
    void commonEditorSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonEditorSettings";

        // GUI
        Assertions.assertEquals(Config.toString(EditorCommonSettings.getFontSize()),
                framework.getConfigVar(prefix + ".fontSize", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getTitleStyle()),
                framework.getConfigVar(prefix + ".titleStyle", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getDialogStyle()),
                framework.getConfigVar(prefix + ".dialogStyle", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getRecentCount()),
                framework.getConfigVar(prefix + ".recentCount", false));

        // Canvas
        Assertions.assertEquals(Config.toString(EditorCommonSettings.getRedrawInterval()),
                framework.getConfigVar(prefix + ".redrawInterval", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getBackgroundColor()),
                framework.getConfigVar(prefix + ".backgroundColor", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getPngBackgroundColor()),
                framework.getConfigVar(prefix + ".pngBackgroundColor", false));

        // Grid
        Assertions.assertEquals(Config.toString(EditorCommonSettings.getGridVisibility()),
                framework.getConfigVar(prefix + ".gridVisibility", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getLightGrid()),
                framework.getConfigVar(prefix + ".lightGrid", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getGridColor()),
                framework.getConfigVar(prefix + ".gridColor", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getRulerVisibility()),
                framework.getConfigVar(prefix + ".rulerVisibility", false));

        // Hints
        Assertions.assertEquals(Config.toString(EditorCommonSettings.getHintVisibility()),
                framework.getConfigVar(prefix + ".hintVisibility", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getHintColor()),
                framework.getConfigVar(prefix + ".hintColor", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getIssueVisibility()),
                framework.getConfigVar(prefix + ".issueVisibility", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getIssueColor()),
                framework.getConfigVar(prefix + ".issueColor", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getFlashInterval()),
                framework.getConfigVar(prefix + ".flashInterval", false));

        // Layout
        Assertions.assertEquals(Config.toString(EditorCommonSettings.getOpenNonvisual()),
                framework.getConfigVar(prefix + ".openNonvisual", false));

        Assertions.assertEquals(Config.toString(EditorCommonSettings.getLargeModelSize()),
                framework.getConfigVar(prefix + ".largeModelSize", false));
    }

    @Test
    void commonFavoriteSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonFavoriteSettings";

        Assertions.assertEquals(Config.toString(FavoriteCommonSettings.getFilterFavorites()),
                framework.getConfigVar(prefix + ".filterFavorites", false));
    }

    @Test
    void commonLogSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonLogSettings";

        Assertions.assertEquals(Config.toString(LogCommonSettings.getTextColor()),
                framework.getConfigVar(prefix + ".textColor", false));

        Assertions.assertEquals(Config.toString(LogCommonSettings.getInfoBackground()),
                framework.getConfigVar(prefix + ".infoBackground", false));

        Assertions.assertEquals(Config.toString(LogCommonSettings.getWarningBackground()),
                framework.getConfigVar(prefix + ".warningBackground", false));

        Assertions.assertEquals(Config.toString(LogCommonSettings.getErrorBackground()),
                framework.getConfigVar(prefix + ".errorBackground", false));

        Assertions.assertEquals(Config.toString(LogCommonSettings.getStdoutBackground()),
                framework.getConfigVar(prefix + ".stdoutBackground", false));

        Assertions.assertEquals(Config.toString(LogCommonSettings.getStderrBackground()),
                framework.getConfigVar(prefix + ".stderrBackground", false));
    }

    @Test
    void commonSignalSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonSignalSettings";

        Assertions.assertEquals(Config.toString(SignalCommonSettings.getInputColor()),
                framework.getConfigVar(prefix + ".inputColor", false));

        Assertions.assertEquals(Config.toString(SignalCommonSettings.getOutputColor()),
                framework.getConfigVar(prefix + ".outputColor", false));

        Assertions.assertEquals(Config.toString(SignalCommonSettings.getInternalColor()),
                framework.getConfigVar(prefix + ".internalColor", false));

        Assertions.assertEquals(Config.toString(SignalCommonSettings.getDummyColor()),
                framework.getConfigVar(prefix + ".dummyColor", false));

        Assertions.assertEquals(Config.toString(SignalCommonSettings.getShowToggle()),
                framework.getConfigVar(prefix + ".showToggle", false));

        Assertions.assertEquals(Config.toString(SignalCommonSettings.getGroupByType()),
                framework.getConfigVar(prefix + ".groupByType", false));
    }

    @Test
    void commonVisualSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonVisualSettings";

        // Node
        Assertions.assertEquals(Config.toString(VisualCommonSettings.getNodeSize()),
                framework.getConfigVar(prefix + ".nodeSize", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getStrokeWidth()),
                framework.getConfigVar(prefix + ".strokeWidth", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getBorderColor()),
                framework.getConfigVar(prefix + ".borderColor", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getFillColor()),
                framework.getConfigVar(prefix + ".fillColor", false));

        // Label
        Assertions.assertEquals(Config.toString(VisualCommonSettings.getLineSpacing()),
                framework.getConfigVar(prefix + ".lineSpacing", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getLabelVisibility()),
                framework.getConfigVar(prefix + ".labelVisibility", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getLabelPositioning()),
                framework.getConfigVar(prefix + ".labelPositioning", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getLabelColor()),
                framework.getConfigVar(prefix + ".labelColor", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getLabelFontSize()),
                framework.getConfigVar(prefix + ".labelFontSize", false));

        // Name
        Assertions.assertEquals(Config.toString(VisualCommonSettings.getNameVisibility()),
                framework.getConfigVar(prefix + ".nameVisibility", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getNamePositioning()),
                framework.getConfigVar(prefix + ".namePositioning", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getNameColor()),
                framework.getConfigVar(prefix + ".nameColor", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getNameFontSize()),
                framework.getConfigVar(prefix + ".nameFontSize", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getShowAbsolutePaths()),
                framework.getConfigVar(prefix + ".showAbsolutePaths", false));

        // Connection
        Assertions.assertEquals(Config.toString(VisualCommonSettings.getConnectionLineWidth()),
                framework.getConfigVar(prefix + ".connectionLineWidth", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getConnectionArrowWidth()),
                framework.getConfigVar(prefix + ".connectionArrowWidth", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getConnectionArrowLength()),
                framework.getConfigVar(prefix + ".connectionArrowLength", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getConnectionBubbleSize()),
                framework.getConfigVar(prefix + ".connectionBubbleSize", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getConnectionColor()),
                framework.getConfigVar(prefix + ".connectionColor", false));

        // Pivot
        Assertions.assertEquals(Config.toString(VisualCommonSettings.getPivotSize()),
                framework.getConfigVar(prefix + ".pivotSize", false));

        Assertions.assertEquals(Config.toString(VisualCommonSettings.getPivotWidth()),
                framework.getConfigVar(prefix + ".pivotWidth", false));

        // Expression
        Assertions.assertEquals(Config.toString(VisualCommonSettings.getUseSubscript()),
                framework.getConfigVar(prefix + ".useSubscript", false));
    }

}
