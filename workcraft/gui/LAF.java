package org.workcraft.gui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.utils.SubstanceConstants.TabContentPaneBorderKind;

public class LAF {
	public static String currentLAF = UIManager.getLookAndFeel().getClass().getName();

	public static String getCurrentLAF() {
		return currentLAF;
	}

	public static void setLAF(String LAF) {
		try {
			UIManager.setLookAndFeel(LAF);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		currentLAF = LAF;
	}

	public static void setDefaultLAF() {
		setLAF(UIManager.getCrossPlatformLookAndFeelClassName());
	}

	public static void setSubstanceLAF(String LAF) {
		setLAF(LAF);
		UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
	}
}
