package org.workcraft.plugins.circuit.serialisation;

import org.workcraft.interop.Format;
import org.workcraft.plugins.circuit.Circuit;

import java.io.File;

public interface PathbreakExporter {
    Format getFormat();
    void export(Circuit circuit, File file);
}
