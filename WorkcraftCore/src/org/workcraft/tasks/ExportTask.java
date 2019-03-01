package org.workcraft.tasks;

import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.interop.Exporter;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportTask implements Task<ExportOutput> {

    Exporter exporter;
    Model model;
    File file;

    public ExportTask(Exporter exporter, Model model, String path) {
        this(exporter, model, new File(path));
    }

    public ExportTask(Exporter exporter, Model model, File file) {
        this.exporter = exporter;
        this.model = model;
        this.file = file;
    }

    @Override
    public Result<? extends ExportOutput> run(ProgressMonitor<? super ExportOutput> monitor) {
        String message = "Exporting model ";
        String title = model.getTitle();
        if (!title.isEmpty()) {
            message += "\'" + title + "\' ";
        }
        message += "to file \'" + file.getAbsolutePath() + "\'.";
        LogUtils.logInfo(message);

        FileOutputStream fos;
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            return new Result<>(e);
        }

        boolean success = false;
        try {
            // For incompatible visual model try exporting its underlying math model.
            if ((model instanceof VisualModel) && !exporter.isCompatible(model)) {
                VisualModel visualModel = (VisualModel) model;
                MathModel mathModel = visualModel.getMathModel();
                if (exporter.isCompatible(mathModel)) {
                    model = mathModel;
                } else {
                    String exporterName = exporter.getFormat().getName();
                    String modelName = model.getDisplayName();
                    String text = "Exporter to " + exporterName + " format is not compatible with " + modelName + " model.";
                    // FIXME: Is it really necessary to nest the exceptions?
                    Exception nestedException = new Exception(new RuntimeException(text));
                    return new Result<>(nestedException);
                }
            }
            exporter.export(model, fos);
            success = true;
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                return new Result<>(e);
            }
            if (!success) {
                file.delete();
            }
        }

        return new Result<>(Outcome.SUCCESS, new ExportOutput(file));
    }

}
