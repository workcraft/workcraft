package org.workcraft.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ModelSerialiser;

import java.io.OutputStream;

public abstract class AbstractSerialiseExporter implements Exporter {

    @Override
    public boolean isCompatible(Model model) {
        return getSerialiser().isApplicableTo(model);
    }

    @Override
    public void export(Model model, OutputStream out) throws SerialisationException {
        getSerialiser().serialise(model, out, null);
    }

    public abstract ModelSerialiser getSerialiser();

}
