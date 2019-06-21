package org.workcraft.dom.references;

import org.workcraft.utils.FileUtils;

import java.io.File;

public class FileReference {

    private String base = null;
    private String path = null;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = FileUtils.fixSeparator(base);
        setPath(getPath());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = FileUtils.stripBase(path, base);
    }

    public File getFile() {
        File result = null;
        if (path != null) {
            result = new File(path);
            if (!result.isAbsolute()) {
                result = new File(base, path);
            }
        }
        return result;
    }

}
