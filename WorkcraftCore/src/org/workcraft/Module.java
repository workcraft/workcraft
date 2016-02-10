package org.workcraft;

public interface Module extends Plugin {
    String getDescription();
    void init();
}
