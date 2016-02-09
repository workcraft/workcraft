package org.workcraft.plugins.stg;

import java.io.File;
import java.io.IOException;

import org.workcraft.Framework;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.workspace.Path;
import org.workcraft.serialisation.Format;
import org.workcraft.util.Export;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class DotGProvider {

    public File getDotG (Path<String> source) {
        final Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();
        WorkspaceEntry we = workspace.getOpenFile(source);

        if (we != null) {
            STGModel model;

            ModelEntry modelEntry = we.getModelEntry();

            if (modelEntry.getMathModel() instanceof STGModel)
                model = (STGModel) modelEntry.getMathModel();
            else
                throw new RuntimeException ("Unexpected model class " + we.getClass().getName());
            try {
                String prefix = model.getTitle();
                if ((prefix == null) || prefix.isEmpty()) {
                    prefix = "Untitled";
                }
                File file = File.createTempFile(prefix, ".g");
                Export.exportToFile(model, file, Format.STG, framework.getPluginManager());
                return file;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ModelValidationException e) {
                throw new RuntimeException(e);
            } catch (SerialisationException e) {
                throw new RuntimeException(e);
            }
        } else if (source.getNode().endsWith(".g")){
            return workspace.getFile(source);
        } else if (source.getNode().endsWith(FileFilters.DOCUMENT_EXTENSION)){
            throw new NotImplementedException();
        } else {
            throw new RuntimeException ("Don't know how to create a .g file from " + source);
        }
    }
}