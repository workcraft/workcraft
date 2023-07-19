package org.workcraft.plugins.stg.interop;

import org.workcraft.dom.Model;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.serialisation.SerialiserUtils;

import java.io.OutputStream;

public class StgExporter implements Exporter {

    @Override
    public Format getFormat() {
        return StgFormat.getInstance();
    }

    @Override
    public boolean isCompatible(Model model) {
        return model instanceof PetriModel;
    }

    @Override
    public void serialise(Model model, OutputStream out) {
        SerialiserUtils.writeModel(model, out, getCurrentFile(), SerialiserUtils.Style.STG, false);
    }

}
