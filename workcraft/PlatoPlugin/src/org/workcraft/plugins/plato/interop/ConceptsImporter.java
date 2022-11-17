package org.workcraft.plugins.plato.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.plato.exceptions.PlatoException;
import org.workcraft.plugins.plato.tasks.PlatoResultHandler;
import org.workcraft.plugins.plato.tasks.PlatoTask;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.utils.FileUtils;
import org.workcraft.workspace.ModelEntry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConceptsImporter implements Importer {

    @Override
    public ConceptsFormat getFormat() {
        return ConceptsFormat.getInstance();
    }

    @Override
    public ModelEntry importFrom(InputStream in, String serialisedUserData) throws DeserialisationException {
        try {
            boolean system = FileUtils.containsKeyword(in, "system =");
            File file = FileUtils.createTempFile("plato-", ".hs");
            FileUtils.copyStreamToFile(in, file);
            PlatoTask task = new PlatoTask(file, new String[0], false, system);
            PlatoResultHandler monitor = new PlatoResultHandler(this);
            Result<? extends ExternalProcessOutput> result = task.run(monitor);
            if (result.isSuccess()) {
                String stdout = result.getPayload().getStdoutString();
                if (stdout.startsWith(".model")) {
                    StgImporter importer = new StgImporter();
                    ByteArrayInputStream is = new ByteArrayInputStream(result.getPayload().getStdout());
                    StgModel stg = importer.importStg(is);
                    return new ModelEntry(new StgDescriptor(), stg);
                }
            }
            throw new PlatoException(result);
        } catch (IOException e) {
            throw new DeserialisationException(e);
        } catch (PlatoException e) {
            e.handleConceptsError();
            throw new DeserialisationException();
        }
    }

}
