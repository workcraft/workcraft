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

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceConstants.TabContentPaneBorderKind;


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
