package org.workcraft.interop;
import java.io.FileFilter;

import org.workcraft.Plugin;
import org.workcraft.dom.Model;

public interface Converter extends Plugin, FileFilter {
    Class<? extends Model> getSourceModelClass();
    Class<? extends Model> getTargetModelClass();
    boolean supportsVisual();
    Model convert(Model source);
}
