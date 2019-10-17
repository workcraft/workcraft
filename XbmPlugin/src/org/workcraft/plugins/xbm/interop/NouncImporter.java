package org.workcraft.plugins.xbm.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.interop.Format;
import org.workcraft.interop.Importer;
import org.workcraft.workspace.ModelEntry;

import java.io.IOException;
import java.io.InputStream;

public class NouncImporter implements Importer {

    @Override
    public Format getFormat() {
        return null;
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws IOException, DeserialisationException, OperationCancelledException {
        return null;
    }
}
