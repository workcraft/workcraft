package org.workcraft.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class SvgFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        if (f.getName().endsWith(".svg")) {
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Scalable Vector Graphics (*.svg)";
    }

}