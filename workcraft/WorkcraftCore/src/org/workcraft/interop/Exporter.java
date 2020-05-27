package org.workcraft.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;

import java.io.OutputStream;

public interface Exporter {
    Format getFormat();
    boolean isCompatible(Model model);
    void export(Model model, OutputStream out) throws ModelValidationException, SerialisationException;
}
