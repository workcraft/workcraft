package org.workcraft.interop;
import java.io.IOException;
import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.workspace.ModelEntry;

public interface Importer {
    Format getFormat();
    ModelEntry importFrom(InputStream in) throws DeserialisationException, IOException;
}
