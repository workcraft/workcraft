package org.workcraft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.workcraft.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.PluginInfo;
import org.workcraft.plugins.shared.tasks.ExportTask;

public class Export {

    public static Exporter chooseBestExporter(PluginProvider provider, Model model, Format format) {
        return chooseBestExporter(provider, model, format.getName(), format.getUuid());
    }

    public static Exporter chooseBestExporter(PluginProvider provider, Model model, String formatName, UUID formatUuid) {
        Iterable<PluginInfo<? extends Exporter>> plugins = provider.getPlugins(Exporter.class);
        for (PluginInfo<? extends Exporter> info : plugins) {
            Exporter exporter = info.getSingleton();
            if (exporter.isCompatible(model)) {
                Format exporterFormat = exporter.getFormat();
                boolean formatMatchByName = (formatName != null) && formatName.equalsIgnoreCase(exporterFormat.getName());
                boolean formatMatchByUuid = (formatUuid != null) && formatUuid.equals(exporterFormat.getUuid());
                if (formatMatchByName || formatMatchByUuid) {
                    return exporter;
                }
            }
        }
        return null;
    }

    public static void exportToFile(Model model, File file, Format format, PluginProvider provider)
            throws IOException, ModelValidationException, SerialisationException {
        Exporter exporter = chooseBestExporter(provider, model, format);
        if (exporter == null) {
            throw new SerialisationException("No exporter available for model type " + model.getDisplayName()
                    + " to produce format " + format.getDescription());
        }
        exportToFile(exporter, model, file);
    }

    public static ExportTask createExportTask(Model model, File file, Format format, PluginProvider provider)
            throws SerialisationException {
        Exporter exporter = chooseBestExporter(provider, model, format);
        if (exporter == null) {
            throw new SerialisationException("No exporter available for model type " + model.getDisplayName()
                    + " to produce format " + format.getDescription());
        }
        return new ExportTask(exporter, model, file.getAbsolutePath());
    }

    public static void exportToFile(Exporter exporter, Model model, File file)
            throws IOException, ModelValidationException, SerialisationException {

        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        boolean success = false;
        try {
            // For incompatible visual model try exporting its underlying math model.
            if ((model instanceof VisualModel) && !exporter.isCompatible(model)) {
                MathModel mathModel = ((VisualModel) model).getMathModel();
                if (exporter.isCompatible(mathModel)) {
                    model = mathModel;
                } else {
                    String exporterName = exporter.getFormat().getDescription();
                    String modelName = model.getDisplayName();
                    String text = "Exporter to " + exporterName + " is not compatible with " + modelName + " model.";
                    throw new RuntimeException(text);
                }
            }
            exporter.export(model, fos);
            success = true;
        } finally {
            fos.close();
            if (!success) {
                file.delete();
            }
        }
    }

    public static void exportToFile(Exporter exporter, Model model, String fileName)
            throws IOException, ModelValidationException, SerialisationException {
        exportToFile(exporter, model, new File(fileName));
    }

}
