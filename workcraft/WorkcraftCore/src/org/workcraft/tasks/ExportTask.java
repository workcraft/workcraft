package org.workcraft.tasks;

import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.interop.Exporter;
import org.workcraft.utils.ExportUtils;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.IOException;

public class ExportTask implements Task<ExportOutput> {

    private final Exporter exporter;
    private final Model model;
    private final File file;

    public ExportTask(Exporter exporter, Model model, File file) {
        this.exporter = exporter;
        this.model = model;
        this.file = file;
    }

    @Override
    public Result<? extends ExportOutput> run(ProgressMonitor<? super ExportOutput> monitor) {
        LogUtils.logInfo(ExportUtils.getExportMessage(model, file));

        try {
            file.createNewFile();
        } catch (IOException e) {
            return new Result<>(e);
        }

        boolean success = false;
        try {
            // For incompatible visual model try exporting its underlying math model.
            Model exportModel = model;
            if ((exportModel instanceof VisualModel visualModel) && !exporter.isCompatible(exportModel)) {
                MathModel mathModel = visualModel.getMathModel();
                if (exporter.isCompatible(mathModel)) {
                    exportModel = mathModel;
                } else {
                    String exporterName = exporter.getFormat().getName();
                    String modelName = visualModel.getDisplayName();
                    String text = "Exporter to " + exporterName + " format is not compatible with " + modelName + " model.";
                    // FIXME: Is it really necessary to nest the exceptions?
                    Exception nestedException = new Exception(new RuntimeException(text));
                    return new Result<>(nestedException);
                }
            }
            exporter.exportToFile(exportModel, file);
            success = true;
        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            if (!success) {
                file.delete();
            }
        }

        return Result.success(new ExportOutput(file));
    }

}
