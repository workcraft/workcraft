package org.workcraft.dom.references;

import org.workcraft.utils.FileUtils;

import java.io.File;

public class FileReference {

    private String base = null;
    private String path = null;

    public FileReference() {
    }

    public FileReference(File file) {
        this(file == null ? null : file.getPath());
    }

    public FileReference(String path) {
        setPath(path);
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        File file = getFile();
        this.base = FileUtils.appendUnixFileSeparator(FileUtils.useUnixFileSeparator(base));
        if (file != null) {
            setPath(file.getPath());
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = FileUtils.getUnixRelativePath(path, base);
    }

    public File getFile() {
        return FileUtils.getFileByPathAndBase(getPath(), getBase());
    }

}
