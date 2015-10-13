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

package org.workcraft.plugins.shared;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CommonDebugSettings implements Settings {
	private static final LinkedList<PropertyDescriptor> properties = new LinkedList<PropertyDescriptor>();
	private static final String prefix = "CommonDebugSettings";

	private static final String keyCopyModelOnChange = prefix + ".copyModelOnChange";
	private static final String keyKeepTemporaryFiles = prefix + ".keepTemporaryFiles";
	private static final String keyVerboseImport = prefix + ".verboseImport";

	private static final boolean defaultCopyModelOnChange = false;
	private static final Boolean defaultKeepTemporaryFiles = true;
	private static final Boolean defaultVerboseImport = false;

	private static boolean copyModelOnChange = defaultCopyModelOnChange;
	private static Boolean keepTemporaryFiles = defaultKeepTemporaryFiles;
	private static Boolean verboseImport = defaultVerboseImport;

	public CommonDebugSettings() {
		properties.add(new PropertyDeclaration<CommonDebugSettings, Boolean>(
				this, "On modifications copy model to clipboard", Boolean.class, true, false, false) {
			protected void setter(CommonDebugSettings object, Boolean value) {
				setCopyModelOnChange(value);
			}
			protected Boolean getter(CommonDebugSettings object) {
				return getCopyModelOnChange();
			}
		});

		properties.add(new PropertyDeclaration<CommonDebugSettings, Boolean>(
				this, "Keep temporary files till application close", Boolean.class, true, false, false) {
			protected void setter(CommonDebugSettings object, Boolean value) {
				setKeepTemporaryFiles(value);
			}
			protected Boolean getter(CommonDebugSettings object) {
				return getKeepTemporaryFiles();
			}
		});

		properties.add(new PropertyDeclaration<CommonDebugSettings, Boolean>(
				this, "Verbose log on file import", Boolean.class, true, false, false) {
			protected void setter(CommonDebugSettings object, Boolean value) {
				setVerboseImport(value);
			}
			protected Boolean getter(CommonDebugSettings object) {
				return getVerboseImport();
			}
		});
	}

	@Override
	public List<PropertyDescriptor> getDescriptors() {
		return properties;
	}

	@Override
	public void load(Config config) {
		setCopyModelOnChange(config.getBoolean(keyCopyModelOnChange, defaultCopyModelOnChange));
		setKeepTemporaryFiles(config.getBoolean(keyKeepTemporaryFiles, defaultKeepTemporaryFiles));
		setVerboseImport(config.getBoolean(keyVerboseImport, defaultVerboseImport));
	}

	@Override
	public void save(Config config) {
		config.setBoolean(keyCopyModelOnChange, getCopyModelOnChange());
		config.setBoolean(keyKeepTemporaryFiles, getKeepTemporaryFiles());
		config.setBoolean(keyVerboseImport, getVerboseImport());
	}

	@Override
	public String getSection() {
		return "Common";
	}

	@Override
	public String getName() {
		return "Debug";
	}

	public static Boolean getCopyModelOnChange() {
		return copyModelOnChange;
	}

	public static void setCopyModelOnChange(Boolean value) {
		copyModelOnChange = value;
	}

	public static Boolean getKeepTemporaryFiles() {
		return keepTemporaryFiles;
	}

	public static void setKeepTemporaryFiles(Boolean value) {
		keepTemporaryFiles = value;
	}

	public static Boolean getVerboseImport() {
		return verboseImport;
	}

	public static void setVerboseImport(Boolean value) {
		verboseImport = value;
	}

}
