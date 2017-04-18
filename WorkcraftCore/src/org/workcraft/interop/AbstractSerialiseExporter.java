package org.workcraft.interop;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ModelSerialiser;

public abstract class AbstractSerialiseExporter implements Exporter {

    @Override
    public void export(Model model, OutputStream out)
            throws IOException, ModelValidationException, SerialisationException {

        getSerialiser().serialise(model, out, null);
    }

    @Override
    public String getDescription() {
        return getSerialiser().getExtension() + " (" + getSerialiser().getDescription() + ")";
    }

    @Override
    public String getExtenstion() {
        return getSerialiser().getExtension();
    }

    @Override
    public int getCompatibility(Model model) {
        if (getSerialiser().isApplicableTo(model)) {
            return Exporter.BEST_COMPATIBILITY;
        } else {
            return Exporter.NOT_COMPATIBLE;
        }
    }

    @Override
    public abstract UUID getTargetFormat();

    public abstract ModelSerialiser getSerialiser();

}
