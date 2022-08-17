package org.workcraft.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface Exporter {
    Format getFormat();
    boolean isCompatible(Model model);
    void exportTo(Model model, OutputStream out) throws SerialisationException;

    default void exportTo(Model model, File file) throws SerialisationException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            exportTo(model, fileOutputStream);
        } catch (IOException e) {
            throw new SerialisationException(e);
        }
    }

}
