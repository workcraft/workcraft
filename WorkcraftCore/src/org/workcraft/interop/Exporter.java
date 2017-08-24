package org.workcraft.interop;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;

public interface Exporter {
    int NOT_COMPATIBLE = 0;
    int GENERAL_COMPATIBILITY = 1;
    int BEST_COMPATIBILITY = 10;

    Format getFormat();
    int getCompatibility(Model model);
    void export(Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException;
}
