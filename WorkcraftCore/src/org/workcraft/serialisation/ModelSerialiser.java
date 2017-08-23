package org.workcraft.serialisation;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;

public interface ModelSerialiser extends SerialFormat {
    boolean isApplicableTo(Model model);
    ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) throws SerialisationException;
}
