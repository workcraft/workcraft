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

        Assert.assertEquals(Config.toString(CommonCommentSettings.getBaseSize()),
                framework.getConfigVar(prefix + ".baseSize", false));

        Assert.assertEquals(Config.toString(CommonCommentSettings.getStrokeWidth()),
                framework.getConfigVar(prefix + ".strokeWidth", false));

        Assert.assertEquals(Config.toString(CommonCommentSettings.getTextAlignment()),
                framework.getConfigVar(prefix + ".textAlignment", false));

        Assert.assertEquals(Config.toString(CommonCommentSettings.getTextColor()),
                framework.getConfigVar(prefix + ".textColor", false));

        Assert.assertEquals(Config.toString(CommonCommentSettings.getBorderColor()),
                framework.getConfigVar(prefix + ".borderColor", false));

        Assert.assertEquals(Config.toString(CommonCommentSettings.getFillColor()),
                framework.getConfigVar(prefix + ".fillColor", false));

        Assert.assertEquals(Config.toString(CommonCommentSettings.getFontSize()),
                framework.getConfigVar(prefix + ".fontSize", false));
    }

    @Test
    public void commonDebugSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonDebugSettings";

        Assert.assertEquals(Config.toString(CommonDebugSettings.getCopyModelOnChange()),
                framework.getConfigVar(prefix + ".copyModelOnChange", false));

        Assert.assertEquals(Config.toString(CommonDebugSettings.getVerboseImport()),
                framework.getConfigVar(prefix + ".verboseImport", false));

        Assert.assertEquals(Config.toString(CommonDebugSettings.getParserTracing()),
                framework.getConfigVar(prefix + ".parserTracing", false));

        Assert.assertEquals(Config.toString(CommonDebugSettings.getVerboseCompatibilityManager()),
                framework.getConfigVar(prefix + ".verboseCompatibilityManager", false));

        Assert.assertEquals(Config.toString(CommonDebugSettings.getShortExportHeader()),
                framework.getConfigVar(prefix + ".shortExportHeader", false));
    }

    @Test
    public void commonEditorSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonEditorSettings";

        Assert.assertEquals(Config.toString(CommonEditorSettings.getBackgroundColor()),
                framework.getConfigVar(prefix + ".backgroundColor", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getGridVisibility()),
                framework.getConfigVar(prefix + ".gridVisibility", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getLightGrid()),
                framework.getConfigVar(prefix + ".lightGrid", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getLightGridSize()),
                framework.getConfigVar(prefix + ".lightGridSize", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getGridColor()),
                framework.getConfigVar(prefix + ".gridColor", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getRulerVisibility()),
                framework.getConfigVar(prefix + ".rulerVisibility", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getHintVisibility()),
                framework.getConfigVar(prefix + ".hintVisibility", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getHintColor()),
                framework.getConfigVar(prefix + ".hintColor", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getIssueVisibility()),
                framework.getConfigVar(prefix + ".issueVisibility", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getIssueColor()),
                framework.getConfigVar(prefix + ".issueColor", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getFlashInterval()),
                framework.getConfigVar(prefix + ".flashInterval", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getRecentCount()),
                framework.getConfigVar(prefix + ".recentCount", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getTitleStyle()),
                framework.getConfigVar(prefix + ".titleStyle", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getShowAbsolutePaths()),
                framework.getConfigVar(prefix + ".showAbsolutePaths", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getOpenNonvisual()),
                framework.getConfigVar(prefix + ".openNonvisual", false));

        Assert.assertEquals(Config.toString(CommonEditorSettings.getRedrawInterval()),
                framework.getConfigVar(prefix + ".redrawInterval", false));
    }

    @Test
    public void commonFavoriteSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonFavoriteSettings";

        Assert.assertEquals(Config.toString(CommonFavoriteSettings.getFilterFavorites()),
                framework.getConfigVar(prefix + ".filterFavorites", false));
    }

    @Test
    public void commonLogSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonLogSettings";

        Assert.assertEquals(Config.toString(CommonLogSettings.getTextColor()),
                framework.getConfigVar(prefix + ".textColor", false));

        Assert.assertEquals(Config.toString(CommonLogSettings.getInfoBackground()),
                framework.getConfigVar(prefix + ".infoBackground", false));

        Assert.assertEquals(Config.toString(CommonLogSettings.getWarningBackground()),
                framework.getConfigVar(prefix + ".warningBackground", false));

        Assert.assertEquals(Config.toString(CommonLogSettings.getErrorBackground()),
                framework.getConfigVar(prefix + ".errorBackground", false));

        Assert.assertEquals(Config.toString(CommonLogSettings.getStdoutBackground()),
                framework.getConfigVar(prefix + ".stdoutBackground", false));

        Assert.assertEquals(Config.toString(CommonLogSettings.getStderrBackground()),
                framework.getConfigVar(prefix + ".stderrBackground", false));
    }

    @Test
    public void commonSignalSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonSignalSettings";

        Assert.assertEquals(Config.toString(CommonSignalSettings.getInputColor()),
                framework.getConfigVar(prefix + ".inputColor", false));

        Assert.assertEquals(Config.toString(CommonSignalSettings.getOutputColor()),
                framework.getConfigVar(prefix + ".outputColor", false));

        Assert.assertEquals(Config.toString(CommonSignalSettings.getInternalColor()),
                framework.getConfigVar(prefix + ".internalColor", false));

        Assert.assertEquals(Config.toString(CommonSignalSettings.getDummyColor()),
                framework.getConfigVar(prefix + ".dummyColor", false));

        Assert.assertEquals(Config.toString(CommonSignalSettings.getShowToggle()),
                framework.getConfigVar(prefix + ".showToggle", false));
    }

    @Test
    public void commonVisualSettingsTest() {
        final Framework framework = Framework.getInstance();
        String prefix = "CommonVisualSettings";

        Assert.assertEquals(Config.toString(CommonVisualSettings.getFontSize()),
                framework.getConfigVar(prefix + ".fontSize", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getNodeSize()),
                framework.getConfigVar(prefix + ".nodeSize", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getStrokeWidth()),
                framework.getConfigVar(prefix + ".strokeWidth", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getBorderColor()),
                framework.getConfigVar(prefix + ".borderColor", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getFillColor()),
                framework.getConfigVar(prefix + ".fillColor", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getPivotSize()),
                framework.getConfigVar(prefix + ".pivotSize", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getPivotWidth()),
                framework.getConfigVar(prefix + ".pivotWidth", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getLineSpacing()),
                framework.getConfigVar(prefix + ".lineSpacing", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getLabelVisibility()),
                framework.getConfigVar(prefix + ".labelVisibility", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getLabelPositioning()),
                framework.getConfigVar(prefix + ".labelPositioning", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getLabelColor()),
                framework.getConfigVar(prefix + ".labelColor", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getLabelFontSize()),
                framework.getConfigVar(prefix + ".labelFontSize", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getNameVisibility()),
                framework.getConfigVar(prefix + ".nameVisibility", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getNamePositioning()),
                framework.getConfigVar(prefix + ".namePositioning", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getNameColor()),
                framework.getConfigVar(prefix + ".nameColor", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getNameFontSize()),
                framework.getConfigVar(prefix + ".nameFontSize", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getConnectionLineWidth()),
                framework.getConfigVar(prefix + ".connectionLineWidth", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getConnectionArrowWidth()),
                framework.getConfigVar(prefix + ".connectionArrowWidth", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getConnectionArrowLength()),
                framework.getConfigVar(prefix + ".connectionArrowLength", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getConnectionBubbleSize()),
                framework.getConfigVar(prefix + ".connectionBubbleSize", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getConnectionColor()),
                framework.getConfigVar(prefix + ".connectionColor", false));

        Assert.assertEquals(Config.toString(CommonVisualSettings.getUseSubscript()),
                framework.getConfigVar(prefix + ".useSubscript", false));
    }

}
