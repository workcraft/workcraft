package org.workcraft.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.workspace.ModelEntry;

import java.io.IOException;
import java.io.InputStream;

public interface Importer {
    Format getFormat();
    ModelEntry importFrom(InputStream in) throws IOException, DeserialisationException, OperationCancelledException;
}
