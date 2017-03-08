package org.workcraft.plugins.plato.interop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.plato.tasks.ConceptsTask;
import org.workcraft.plugins.plato.tasks.PlatoResultHandler;
import org.workcraft.plugins.plato.exceptions.PlatoException;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.ModelEntry;

public class PlatoImporter implements Importer {

    private File inputFile;

    @Override
    public boolean accept(File file) {
        if (file.getName().endsWith(".hs")) {
            inputFile = file;
            return true;
        }
        return false;
    }

    @Override
    public String getDescription() {
        return "Concepts file (.hs)";
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        try {
            ConceptsTask task = new ConceptsTask(inputFile);
            PlatoResultHandler monitor = new PlatoResultHandler(null);
            Result<? extends ExternalProcessResult> result = task.run(monitor);
            if (result.getOutcome() == Outcome.FINISHED) {
                String output = new String(result.getReturnValue().getOutput());
                if (output.startsWith(".model")) {
                    DotGImporter importer = new DotGImporter();
                    ByteArrayInputStream is = new ByteArrayInputStream(result.getReturnValue().getOutput());
                    StgModel stg = importer.importSTG(is);
                    return new ModelEntry(new StgDescriptor(), (Model) stg);
                }
            }
            throw new PlatoException(result);
        } catch (PlatoException e) {
            e.handleConceptsError();
            throw new DeserialisationException();
        }
    }

}
