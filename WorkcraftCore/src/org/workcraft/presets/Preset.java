package org.workcraft.presets;

public class Preset<T> {

    private String description;
    private T data;
    private final boolean builtIn;

    public Preset(String description, T data, boolean builtIn) {
        this.description = description;
        this.data = data;
        this.builtIn = builtIn;
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

    public boolean isBuiltIn() {
        return builtIn;
    }

    @Override
    public String toString() {
        return getDescription();
    }

}
