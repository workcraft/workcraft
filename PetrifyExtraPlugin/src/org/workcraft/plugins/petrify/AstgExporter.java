/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.petrify;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.petrify.tasks.DrawAstgTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;

public class AstgExporter implements Exporter {

    private static final UUID FORMAT = DesktopApi.getOs().isWindows() ? Format.PS : Format.PDF;
    private static final String EXTENSION = DesktopApi.getOs().isWindows() ? ".ps" : ".pdf";
    private static final String DESCRIPTION = EXTENSION + " (Petrify)";
    private static final String RESULT_FILE_NAME = "model" + EXTENSION;
    private static final String STG_FILE_NAME = "model.g";

    public void export(Model model, OutputStream out) throws IOException,
            ModelValidationException, SerialisationException {

        if (model == null) {
            throw new IllegalArgumentException("Model is null");
        }

        String prefix = FileUtils.getTempPrefix(null);
        File directory = FileUtils.createTempDirectory(prefix);
        File stgFile = new File(directory, STG_FILE_NAME);

        final Framework framework = Framework.getInstance();
        PluginManager pluginManager = framework.getPluginManager();
        ExportTask exportTask = Export.createExportTask(model, stgFile, Format.STG, pluginManager);
        final Result<? extends Object> result = framework.getTaskManager().execute(exportTask, "Exporting to .g");

        if (result.getOutcome() != Outcome.FINISHED) {
            if (result.getOutcome() == Outcome.CANCELLED) {
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

        final Result<? extends ExternalProcessResult> drawAstgResult = framework.getTaskManager().execute(task, "Executing Petrify");

        if (drawAstgResult.getOutcome() != Outcome.FINISHED) {
            if (drawAstgResult.getOutcome() == Outcome.CANCELLED) {
                return;
            } else {
                if (drawAstgResult.getCause() != null) {
                    throw new SerialisationException(drawAstgResult.getCause());
                } else {
                    throw new SerialisationException("Petrify failed with return code " + drawAstgResult.getReturnValue().getReturnCode() + "\n\n" +
                            new String(drawAstgResult.getReturnValue().getErrors()) + "\n");
                }
            }
        }
        FileUtils.copyFileToStream(resultFile, out);
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    public String getExtenstion() {
        return EXTENSION;
    }

    public int getCompatibility(Model model) {
        if (model instanceof StgModel) {
            return Exporter.GENERAL_COMPATIBILITY;
        } else {
            return Exporter.NOT_COMPATIBLE;
        }
    }

    @Override
    public UUID getTargetFormat() {
        return FORMAT;
    }

}
