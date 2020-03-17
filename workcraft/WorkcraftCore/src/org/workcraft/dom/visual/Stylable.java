package org.workcraft.dom.visual;

public interface Stylable {
    void copyStyle(Stylable src);
    void mixStyle(Stylable... srcs);
}
