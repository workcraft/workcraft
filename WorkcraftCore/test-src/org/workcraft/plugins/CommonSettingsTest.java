package org.workcraft.plugins;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.workcraft.Config;
import org.workcraft.Framework;
import org.workcraft.plugins.builtin.settings.*;

public class CommonSettingsTest {

    @BeforeClass
    public static void init() {
        final Framework framework = Framework.getInstance();
        framework.init();
        framework.resetConfig();
    }

    @Test
    public void commonCommentSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonCommentSettings";

        Assert.assertEquals(Config.toString(CommentCommonSettings.getBaseSize()),
                framework.getConfigVar(prefix + ".baseSize", false));

        Assert.assertEquals(Config.toString(CommentCommonSettings.getStrokeWidth()),
                framework.getConfigVar(prefix + ".strokeWidth", false));

        Assert.assertEquals(Config.toString(CommentCommonSettings.getTextAlignment()),
                framework.getConfigVar(prefix + ".textAlignment", false));

        Assert.assertEquals(Config.toString(CommentCommonSettings.getTextColor()),
                framework.getConfigVar(prefix + ".textColor", false));

        Assert.assertEquals(Config.toString(CommentCommonSettings.getBorderColor()),
                framework.getConfigVar(prefix + ".borderColor", false));

        Assert.assertEquals(Config.toString(CommentCommonSettings.getFillColor()),
                framework.getConfigVar(prefix + ".fillColor", false));

        Assert.assertEquals(Config.toString(CommentCommonSettings.getFontSize()),
                framework.getConfigVar(prefix + ".fontSize", false));
    }

    @Test
    public void commonDebugSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonDebugSettings";

        Assert.assertEquals(Config.toString(DebugCommonSettings.getCopyModelOnChange()),
                framework.getConfigVar(prefix + ".copyModelOnChange", false));

        Assert.assertEquals(Config.toString(DebugCommonSettings.getVerboseImport()),
                framework.getConfigVar(prefix + ".verboseImport", false));

        Assert.assertEquals(Config.toString(DebugCommonSettings.getParserTracing()),
                framework.getConfigVar(prefix + ".parserTracing", false));

        Assert.assertEquals(Config.toString(DebugCommonSettings.getVerboseCompatibilityManager()),
                framework.getConfigVar(prefix + ".verboseCompatibilityManager", false));

        Assert.assertEquals(Config.toString(DebugCommonSettings.getShortExportHeader()),
                framework.getConfigVar(prefix + ".shortExportHeader", false));
    }

    @Test
    public void commonEditorSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonEditorSettings";

        Assert.assertEquals(Config.toString(EditorCommonSettings.getBackgroundColor()),
                framework.getConfigVar(prefix + ".backgroundColor", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getGridVisibility()),
                framework.getConfigVar(prefix + ".gridVisibility", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getLightGrid()),
                framework.getConfigVar(prefix + ".lightGrid", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getLightGridSize()),
                framework.getConfigVar(prefix + ".lightGridSize", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getGridColor()),
                framework.getConfigVar(prefix + ".gridColor", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getRulerVisibility()),
                framework.getConfigVar(prefix + ".rulerVisibility", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getHintVisibility()),
                framework.getConfigVar(prefix + ".hintVisibility", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getHintColor()),
                framework.getConfigVar(prefix + ".hintColor", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getIssueVisibility()),
                framework.getConfigVar(prefix + ".issueVisibility", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getIssueColor()),
                framework.getConfigVar(prefix + ".issueColor", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getFlashInterval()),
                framework.getConfigVar(prefix + ".flashInterval", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getRecentCount()),
                framework.getConfigVar(prefix + ".recentCount", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getTitleStyle()),
                framework.getConfigVar(prefix + ".titleStyle", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getShowAbsolutePaths()),
                framework.getConfigVar(prefix + ".showAbsolutePaths", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getOpenNonvisual()),
                framework.getConfigVar(prefix + ".openNonvisual", false));

        Assert.assertEquals(Config.toString(EditorCommonSettings.getRedrawInterval()),
                framework.getConfigVar(prefix + ".redrawInterval", false));
    }

    @Test
    public void commonFavoriteSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonFavoriteSettings";

        Assert.assertEquals(Config.toString(FavoriteCommonSettings.getFilterFavorites()),
                framework.getConfigVar(prefix + ".filterFavorites", false));
    }

    @Test
    public void commonLogSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonLogSettings";

        Assert.assertEquals(Config.toString(LogCommonSettings.getTextColor()),
                framework.getConfigVar(prefix + ".textColor", false));

        Assert.assertEquals(Config.toString(LogCommonSettings.getInfoBackground()),
                framework.getConfigVar(prefix + ".infoBackground", false));

        Assert.assertEquals(Config.toString(LogCommonSettings.getWarningBackground()),
                framework.getConfigVar(prefix + ".warningBackground", false));

        Assert.assertEquals(Config.toString(LogCommonSettings.getErrorBackground()),
                framework.getConfigVar(prefix + ".errorBackground", false));

        Assert.assertEquals(Config.toString(LogCommonSettings.getStdoutBackground()),
                framework.getConfigVar(prefix + ".stdoutBackground", false));

        Assert.assertEquals(Config.toString(LogCommonSettings.getStderrBackground()),
                framework.getConfigVar(prefix + ".stderrBackground", false));
    }

    @Test
    public void commonSignalSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonSignalSettings";

        Assert.assertEquals(Config.toString(SignalCommonSettings.getInputColor()),
                framework.getConfigVar(prefix + ".inputColor", false));

        Assert.assertEquals(Config.toString(SignalCommonSettings.getOutputColor()),
                framework.getConfigVar(prefix + ".outputColor", false));

        Assert.assertEquals(Config.toString(SignalCommonSettings.getInternalColor()),
                framework.getConfigVar(prefix + ".internalColor", false));

        Assert.assertEquals(Config.toString(SignalCommonSettings.getDummyColor()),
                framework.getConfigVar(prefix + ".dummyColor", false));

        Assert.assertEquals(Config.toString(SignalCommonSettings.getShowToggle()),
                framework.getConfigVar(prefix + ".showToggle", false));
    }

    @Test
    public void commonVisualSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonVisualSettings";

        Assert.assertEquals(Config.toString(VisualCommonSettings.getFontSize()),
                framework.getConfigVar(prefix + ".fontSize", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getNodeSize()),
                framework.getConfigVar(prefix + ".nodeSize", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getStrokeWidth()),
                framework.getConfigVar(prefix + ".strokeWidth", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getBorderColor()),
                framework.getConfigVar(prefix + ".borderColor", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getFillColor()),
                framework.getConfigVar(prefix + ".fillColor", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getPivotSize()),
                framework.getConfigVar(prefix + ".pivotSize", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getPivotWidth()),
                framework.getConfigVar(prefix + ".pivotWidth", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getLineSpacing()),
                framework.getConfigVar(prefix + ".lineSpacing", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getLabelVisibility()),
                framework.getConfigVar(prefix + ".labelVisibility", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getLabelPositioning()),
                framework.getConfigVar(prefix + ".labelPositioning", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getLabelColor()),
                framework.getConfigVar(prefix + ".labelColor", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getLabelFontSize()),
                framework.getConfigVar(prefix + ".labelFontSize", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getNameVisibility()),
                framework.getConfigVar(prefix + ".nameVisibility", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getNamePositioning()),
                framework.getConfigVar(prefix + ".namePositioning", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getNameColor()),
                framework.getConfigVar(prefix + ".nameColor", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getNameFontSize()),
                framework.getConfigVar(prefix + ".nameFontSize", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getConnectionLineWidth()),
                framework.getConfigVar(prefix + ".connectionLineWidth", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getConnectionArrowWidth()),
                framework.getConfigVar(prefix + ".connectionArrowWidth", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getConnectionArrowLength()),
                framework.getConfigVar(prefix + ".connectionArrowLength", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getConnectionBubbleSize()),
                framework.getConfigVar(prefix + ".connectionBubbleSize", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getConnectionColor()),
                framework.getConfigVar(prefix + ".connectionColor", false));

        Assert.assertEquals(Config.toString(VisualCommonSettings.getUseSubscript()),
                framework.getConfigVar(prefix + ".useSubscript", false));
    }

}
