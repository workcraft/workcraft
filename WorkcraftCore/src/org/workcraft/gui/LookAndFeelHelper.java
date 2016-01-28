/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
		//String laf = UIManager.getSystemLookAndFeelClassName();
		setLookAndFeel(laf);
	}

	public static void setLookAndFeel(String laf) {
		try {
			UIManager.setLookAndFeel(laf);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	public static void setSubstance(String laf) {
		setLookAndFeel(laf);
		UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CONTENT_BORDER_KIND, TabContentPaneBorderKind.SINGLE_FULL);
	}
}
