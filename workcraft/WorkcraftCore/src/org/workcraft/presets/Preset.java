package org.workcraft.presets;

import java.io.File;

public class Preset<T> {

    private String description;
    private T data;
    private File file;

    public Preset(String description, T data) {
        this.description = description;
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public T getData() {
        return data;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setData(T data) {
        this.data = data;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

}
