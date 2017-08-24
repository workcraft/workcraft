package org.workcraft.interop;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ModelSerialiser;

public abstract class AbstractSerialiseExporter implements Exporter {

    @Override
    public int getCompatibility(Model model) {
        if (getSerialiser().isApplicableTo(model)) {
            return Exporter.BEST_COMPATIBILITY;
        } else {
            return Exporter.NOT_COMPATIBLE;
        }
    }

    @Override
    public void export(Model model, OutputStream out)
            throws IOException, ModelValidationException, SerialisationException {

        getSerialiser().serialise(model, out, null);
    }

    public abstract ModelSerialiser getSerialiser();

}
