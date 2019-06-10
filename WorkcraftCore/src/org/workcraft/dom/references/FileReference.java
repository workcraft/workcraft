package org.workcraft.dom.references;

import java.io.File;

public class FileReference {

    private String base = null;
    private String path = null;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = fixSeparator(base);
        setPath(getPath());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = stripBase(path);
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

    public String stripBase(String path) {
        path = fixSeparator(path);
        if ((base != null) && (path != null) && path.startsWith(base)) {
            String result = path.substring(base.length());
            while (result.startsWith("/")) {
                result = result.substring(1);
            }
            return result;
        }
        return path;
    }

    public static String fixSeparator(String path) {
        if (path != null) {
            return path.replace("\\", "/");
        }
        return null;
    }

}
