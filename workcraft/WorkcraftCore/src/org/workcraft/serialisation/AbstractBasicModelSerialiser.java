package org.workcraft.serialisation;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;

import java.io.OutputStream;

public abstract class AbstractBasicModelSerialiser implements ModelSerialiser<Void, Void> {

    @Override
    public final Void serialise(Model model, OutputStream out, Void userData) throws SerialisationException {
        serialise(model, out);
        return null;
    }

    public abstract void serialise(Model model, OutputStream out) throws SerialisationException;

}
