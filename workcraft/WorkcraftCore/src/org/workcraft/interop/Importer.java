package org.workcraft.interop;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.utils.FileUtils;
import org.workcraft.workspace.ModelEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Importer {
    Format getFormat();
    ModelEntry importFrom(InputStream in) throws DeserialisationException, OperationCancelledException;

    default ModelEntry importFrom(File file) throws DeserialisationException, OperationCancelledException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            ModelEntry me = importFrom(fileInputStream);
            if (me.getDesiredName() == null) {
                me.setDesiredName(FileUtils.getFileNameWithoutExtension(file));
            }
            return me;
        } catch (IOException e) {
            throw new DeserialisationException(e);
        }
    }

}
