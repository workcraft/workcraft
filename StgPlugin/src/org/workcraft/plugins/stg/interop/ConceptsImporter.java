package org.workcraft.plugins.stg.interop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.concepts.ConceptsResultHandler;
import org.workcraft.plugins.stg.concepts.ConceptsTask;
import org.workcraft.plugins.stg.concepts.ConceptsToolException;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;

public class ConceptsImporter implements Importer {

    private static final String GRAPH_KEYWORD = "module Concept where";
    private File inputFile;

    @Override
    public boolean accept(File file) {
        if (file.getName().endsWith(".hs")
                && FileUtils.fileContainsKeyword(file, GRAPH_KEYWORD)) {
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
            Result<? extends ExternalProcessResult> result = importConcepts();
            if (result.getOutcome() == Outcome.FINISHED) {
                String output = new String(result.getReturnValue().getOutput());
                if (output.startsWith(".model")) {
                    DotGImporter importer = new DotGImporter();
                    ByteArrayInputStream is = new ByteArrayInputStream(result.getReturnValue().getOutput());
                    StgModel stg = importer.importSTG(is);
                    return new ModelEntry(new StgDescriptor(), stg);
                }
            }
            throw new ConceptsToolException(result);
        } catch (ConceptsToolException e) {
            e.handleConceptsError();
            throw new DeserialisationException();
        }
    }

    public Result<? extends ExternalProcessResult> importConcepts() throws DeserialisationException {
        ConceptsTask task = new ConceptsTask(inputFile);
        ConceptsResultHandler resultHandler = new ConceptsResultHandler(null);
        return task.run(resultHandler);
    }

}
