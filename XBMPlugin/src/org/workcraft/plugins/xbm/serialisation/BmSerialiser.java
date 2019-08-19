package org.workcraft.plugins.xbm.serialisation;

import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.xbm.Xbm;
import org.workcraft.plugins.xbm.interop.BmFormat;
import org.workcraft.serialisation.ModelSerialiser;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.utils.ExportUtils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;

public class BmSerialiser implements ModelSerialiser {

    @Override
    public ReferenceProducer serialise(Model model, OutputStream out, ReferenceProducer refs) throws SerialisationException {

        PrintWriter writer = new PrintWriter(out);
        writer.write(Info.getGeneratedByText("# BM file", "\n"));
        String title = ExportUtils.asIdentifier(model.getTitle());
        writer.write("Name " + title + "\n\n");
        if (model instanceof Xbm) {
            writeXbm(writer, (Xbm) model);
        } else {
            throw new ArgumentException("Model class not supported: " + model.getClass().getName());
        }
        writer.close();
        return refs;
    }

    @Override
    public boolean isApplicableTo(Model model) {
        return model instanceof Xbm;
    }

    @Override
    public UUID getFormatUUID() {
        return BmFormat.getInstance().getUuid();
    }

    private void writeXbm(PrintWriter writer, Xbm xbm) {

    }
}
