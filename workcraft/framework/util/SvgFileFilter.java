package org.workcraft.framework.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SvgFileFilter extends FileFilter {


	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		if (f.getName().endsWith(".svg"))
			return true;
		return false;
	}


	public String getDescription() {
		return "Scalable Vector Graphics (*.svg)";
	}

}