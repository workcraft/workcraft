package org.workcraft.interop;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;

public interface Exporter {
    Format getFormat();
    boolean isCompatible(Model model);
    void export(Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException;
}
