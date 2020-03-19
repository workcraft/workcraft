package org.workcraft.interop;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.workcraft.utils.FileUtils;

public class FormatFileFilter extends FileFilter {

    private final Format format;

    public FormatFileFilter(Format format) {
        this.format = format;
    }

    @Override
    public boolean accept(File file) {
        return file.isDirectory() || checkFileFormat(file, format);
    }

    @Override
    public String getDescription() {
        return format.getDescription() + " (*" + format.getExtension() + ")";
    }

    public static boolean checkFileFormat(File file, Format format) {
        String name = file.getName();
        String extension = format.getExtension();
        String keyword = format.getKeyword();
        return name.endsWith(extension) && FileUtils.containsKeyword(file, keyword);
    }

}
