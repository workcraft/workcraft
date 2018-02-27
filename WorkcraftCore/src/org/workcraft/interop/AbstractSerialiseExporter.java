package org.workcraft.interop;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ModelSerialiser;

public abstract class AbstractSerialiseExporter implements Exporter {

    @Override
    public boolean isCompatible(Model model) {
        return getSerialiser().isApplicableTo(model);
    }

    @Override
    public void export(Model model, OutputStream out)
            throws IOException, ModelValidationException, SerialisationException {

        getSerialiser().serialise(model, out, null);
    }

    public abstract ModelSerialiser getSerialiser();

}
