package org.workcraft.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.workspace.ModelEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Importer {
    Format getFormat();
    ModelEntry importFrom(InputStream in) throws DeserialisationException, OperationCancelledException;

    default ModelEntry importFrom(File file) throws DeserialisationException, OperationCancelledException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return importFrom(fileInputStream);
        } catch (IOException e) {
            throw new DeserialisationException(e);
        }
    }

}
