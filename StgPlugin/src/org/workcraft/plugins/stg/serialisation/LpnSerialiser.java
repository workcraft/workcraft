package org.workcraft.plugins.stg.serialisation;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.LpnFormat;
import org.workcraft.plugins.stg.serialisation.SerialiserUtils.Style;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;

public class LpnSerialiser implements ModelSerialiser {

    class ReferenceResolver implements ReferenceProducer {
        HashMap<Object, String> refMap = new HashMap<>();

        @Override
        public String getReference(Object obj) {
            return refMap.get(obj);
        }
    }

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs)
            throws SerialisationException {

        SerialiserUtils.writeModel(model, out, Style.LPN);
        return new ReferenceResolver();
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return (model instanceof StgModel) || (model instanceof PetriNetModel);
    }

    @Override
    public UUID getFormatUUID() {
        return LpnFormat.getInstance().getUuid();
    }

}
