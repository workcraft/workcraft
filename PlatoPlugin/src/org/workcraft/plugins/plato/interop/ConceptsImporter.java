package org.workcraft.plugins.plato.interop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.plato.exceptions.PlatoException;
import org.workcraft.plugins.plato.tasks.PlatoResultHandler;
import org.workcraft.plugins.plato.tasks.PlatoTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.ModelEntry;

public class ConceptsImporter implements Importer {

    private File inputFile;

    @Override
    public ConceptsFormat getFormat() {
        return ConceptsFormat.getInstance();
    }

    @Override
    public boolean accept(File file) {
        if (file.getName().endsWith(".hs")) {
            inputFile = file;
            return true;
        }
        return false;
    }

    @Override
    public ModelEntry importFrom(InputStream in) throws DeserialisationException {
        try {
            String text = getFileText();
            boolean system = false;
            if (text.contains("system =")) {
                system = true;
            }
            PlatoTask task = new PlatoTask(inputFile, new String[0], false, system);
            PlatoResultHandler monitor = new PlatoResultHandler(this);
            Result<? extends ExternalProcessResult> result = task.run(monitor);
            if (result.getOutcome() == Outcome.SUCCESS) {
                String output = new String(result.getReturnValue().getOutput());
                if (output.startsWith(".model")) {
                    StgImporter importer = new StgImporter();
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

    private String getFileText() {
        String result = "";
        try {
            Scanner k = new Scanner(inputFile);
            while (k.hasNextLine()) {
                result = result + k.nextLine();
            }
            k.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

}
