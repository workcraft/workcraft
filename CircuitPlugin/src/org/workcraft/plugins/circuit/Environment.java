package org.workcraft.plugins.circuit;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

import java.io.File;

public class Environment extends MathNode {
    public static final String PROPERTY_FILE = "file";
    public static final String PROPERTY_BASE = "base";
    private File file;
    private File base;

    public File getFile() {
        return file;
    }

    public void setFile(File value) {
        if (file != value) {
            file = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_FILE));
        }
    }

    public File getBase() {
        return base;
    }

    public void setBase(File value) {
        if (base != value) {
            base = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_BASE));
        }
    }

    public String getRelativePath() {
        String result = null;
        if (file != null) {
            result = file.getPath().replace("\\", "/");
        }
        if (base != null) {
            String path = base.getPath().replace("\\", "/");
            if (!path.isEmpty() && result.startsWith(path)) {
                result = result.substring(path.length());
                while (result.startsWith("/")) {
                    result = result.substring(1);
                }
            }
        }
        return result;
    }

}
