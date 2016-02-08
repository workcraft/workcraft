package org.workcraft.dom.visual;

public interface Stylable {
    public void copyStyle(Stylable src);
    public void mixStyle(Stylable... srcs);
}
