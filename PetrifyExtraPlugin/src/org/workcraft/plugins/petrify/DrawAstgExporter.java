package org.workcraft.plugins.petrify;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.interop.PdfFormat;
import org.workcraft.plugins.interop.PsFormat;
import org.workcraft.plugins.petrify.tasks.DrawAstgTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;

public class DrawAstgExporter implements Exporter {

    private static final Format FORMAT = DesktopApi.getOs().isWindows() ? PsFormat.getInstance() : PdfFormat.getInstance();
    private static final String EXTENSION = DesktopApi.getOs().isWindows() ? ".ps" : ".pdf";
    private static final String RESULT_FILE_NAME = "model" + EXTENSION;
    private static final String STG_FILE_NAME = "model.g";

    @Override
    public void export(Model model, OutputStream out) throws IOException,
            ModelValidationException, SerialisationException {

        if (model == null) {
            throw new IllegalArgumentException("Model is null");
        }

        String prefix = FileUtils.getTempPrefix(null);
        File directory = FileUtils.createTempDirectory(prefix);
        File stgFile = new File(directory, STG_FILE_NAME);

        final Framework framework = Framework.getInstance();
        final PluginManager pluginManager = framework.getPluginManager();
        final ExportTask exportTask = Export.createExportTask(model, stgFile, StgFormat.getInstance(), pluginManager);
        final TaskManager taskManager = framework.getTaskManager();
        final Result<? extends Object> result = taskManager.execute(exportTask, "Exporting to .g");

        if (result.getOutcome() != Outcome.SUCCESS) {
            if (result.getOutcome() == Outcome.CANCEL) {
                return;
            } else {
                if (result.getCause() != null) {
                    throw new SerialisationException(result.getCause());
                } else {
                    throw new SerialisationException("Could not export model as .g");
                }
            }
        }

        File resultFile = new File(directory, RESULT_FILE_NAME);

        DrawAstgTask task = new DrawAstgTask(new ArrayList<String>(), stgFile, resultFile, directory);

        final Result<? extends ExternalProcessResult> drawAstgResult = taskManager.execute(task, "Executing Petrify");

        if (drawAstgResult.getOutcome() != Outcome.SUCCESS) {
            if (drawAstgResult.getOutcome() == Outcome.CANCEL) {
                return;
            } else {
                if (drawAstgResult.getCause() != null) {
                    throw new SerialisationException(drawAstgResult.getCause());
                } else {
                    throw new SerialisationException("Petrify failed with return code " +
                            drawAstgResult.getReturnValue().getReturnCode() + "\n\n" +
                            drawAstgResult.getReturnValue().getErrorsHeadAndTail() + "\n");
                }
            }
        }
        FileUtils.copyFileToStream(resultFile, out);
    }

    @Override
    public int getCompatibility(Model model) {
        if (model instanceof StgModel) {
            return Exporter.GENERAL_COMPATIBILITY;
        } else {
            return Exporter.NOT_COMPATIBLE;
        }
    }

    @Override
    public Format getFormat() {
        return FORMAT;
    }

}
