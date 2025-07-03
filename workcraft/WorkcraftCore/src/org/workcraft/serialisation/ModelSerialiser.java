package org.workcraft.serialisation;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;

public interface ModelSerialiser<T, R> extends SerialFormat {
    boolean isApplicableTo(Model<?, ?> model);
    R serialise(Model<?, ?> model, OutputStream out, T userData) throws SerialisationException;
}
