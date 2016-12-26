package org.workcraft.gui;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultEditorKit;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceConstants.TabContentPaneBorderKind;

public class LookAndFeelHelper {

    private static Map<String, String> lafMap;

    public static Map<String, String> getLafMap() {
        if (lafMap == null) {
            lafMap = new LinkedHashMap<>();
            lafMap.put("Metal (default)", "javax.swing.plaf.metal.MetalLookAndFeel");
            lafMap.put("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            lafMap.put("Substance: Moderate", "org.jvnet.substance.skin.SubstanceModerateLookAndFeel");
            lafMap.put("Substance: Mist Silver", "org.jvnet.substance.skin.SubstanceMistSilverLookAndFeel");
            lafMap.put("Substance: Raven", "org.jvnet.substance.skin.SubstanceRavenLookAndFeel");
            lafMap.put("Substance: Business", "org.jvnet.substance.skin.SubstanceBusinessLookAndFeel");
            lafMap.put("Substance: Creme", "org.jvnet.substance.skin.SubstanceCremeCoffeeLookAndFeel");
        }
        return Collections.unmodifiableMap(lafMap);
    }

    public static void setDefaultLookAndFeel() {
        String laf = UIManager.getCrossPlatformLookAndFeelClassName();
        InputMap textFieldInputMap = (InputMap) UIManager.get("TextField.focusInputMap");
        InputMap textAreaInputMap = (InputMap) UIManager.get("TextArea.focusInputMap");
        setLookAndFeel(laf);
        if (DesktopApi.getOs().isMac()) {
            textFieldInputMap.put(KeyStroke.getKeyStroke("UP"), DefaultEditorKit.upAction);
            textAreaInputMap.put(KeyStroke.getKeyStroke("UP"), DefaultEditorKit.upAction);
            textFieldInputMap.put(KeyStroke.getKeyStroke("DOWN"), DefaultEditorKit.downAction);
            textAreaInputMap.put(KeyStroke.getKeyStroke("DOWN"), DefaultEditorKit.downAction);
            UIManager.put("TextField.focusInputMap", textFieldInputMap);
            UIManager.put("TextArea.focusInputMap", textAreaInputMap);
        }
    }

    public static void setLookAndFeel(String laf) {
        try {
            UIManager.setLookAndFeel(laf);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public static void setSubstance(String laf) {
        setLookAndFeel(laf);
        UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
    }
}
