package org.workcraft.workspace;

import java.util.EventListener;

import org.workcraft.dom.Model;

public interface WorkbenchListener extends EventListener {
    void documentLoaded(Model doc);
}
