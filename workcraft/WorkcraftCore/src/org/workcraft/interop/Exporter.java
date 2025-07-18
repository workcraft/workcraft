package org.workcraft.interop;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

public interface Exporter {

    Stack<File> files = new Stack<>();

    Format getFormat();
    boolean isCompatible(Model<?, ?> model);
    void serialise(Model<?, ?> model, OutputStream out) throws SerialisationException;

    default File getCurrentFile() {
        return files.isEmpty() ? null : files.peek();
    }

    default void exportToFile(Model<?, ?> model, File file) throws SerialisationException {
        if (!isCompatible(model)) {
            throw new SerialisationException("Cannot export " + model.getDisplayName()
                    + " as  " + getFormat().getDescription());
        }
        if (FileUtils.checkFileWritability(file)) {
            files.push(file);
            try (FileOutputStream out = new FileOutputStream(file)) {
                serialise(model, out);
            } catch (IOException e) {
                throw new SerialisationException(e);
            } finally {
                if (!files.isEmpty()) {
                    files.pop();
                }
            }
        }
    }

}
