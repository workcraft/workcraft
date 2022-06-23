package org.workcraft.plugins.stg.serialisation;

import org.workcraft.dom.Model;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.serialisation.SerialiserUtils.Style;
import org.workcraft.serialisation.AbstractBasicModelSerialiser;

import java.io.OutputStream;
import java.util.UUID;

public class AstgSerialiser extends AbstractBasicModelSerialiser {

    @Override
    public void serialise(Model model, OutputStream out) {
        SerialiserUtils.writeModel(model, out, Style.STG, false);
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof PetriModel;
    }

    @Override
    public UUID getFormatUUID() {
        return StgFormat.getInstance().getUuid();
    }

}
