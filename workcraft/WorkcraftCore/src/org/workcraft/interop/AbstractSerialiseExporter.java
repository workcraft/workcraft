package org.workcraft.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.AbstractBasicModelSerialiser;

import java.io.OutputStream;

public abstract class AbstractSerialiseExporter implements Exporter {

    @Override
    public boolean isCompatible(Model model) {
        return getSerialiser().isApplicableTo(model);
    }

    @Override
    public void exportTo(Model model, OutputStream out) throws SerialisationException {
        getSerialiser().serialise(model, out);
    }

    public abstract AbstractBasicModelSerialiser getSerialiser();

}
