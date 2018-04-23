package org.workcraft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.swing.JFileChooser;

import org.workcraft.Framework;
import org.workcraft.PluginProvider;
import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.plugins.PluginInfo;

public class ExportUtils {

    public static Exporter chooseBestExporter(PluginProvider provider, Model model, Format format) {
        return chooseBestExporter(provider, model, format.getName(), format.getUuid());
    }

    public static Exporter chooseBestExporter(PluginProvider provider, Model model, String formatName, UUID formatUuid) {
        for (PluginInfo<? extends Exporter> info : provider.getPlugins(Exporter.class)) {
            Exporter exporter = info.getSingleton();
            if (exporter.isCompatible(model)) {
                Format format = exporter.getFormat();
                boolean formatMatchByName = (formatName != null) && formatName.equalsIgnoreCase(format.getName());
                boolean formatMatchByUuid = (formatUuid != null) && formatUuid.equals(format.getUuid());
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
            throw new NoExporterException(model.getDisplayName(), format.getName());
        }
        exportToFile(exporter, model, file);
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

    public static String getValidSavePath(JFileChooser fc, Format format) throws OperationCancelledException {
        String path = null;
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        while (true) {
            if (fc.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                path = fc.getSelectedFile().getPath();
                if (format == null) {
                    if (!path.endsWith(FileFilters.DOCUMENT_EXTENSION)) {
                        path += FileFilters.DOCUMENT_EXTENSION;
                    }
                } else {
                    String extension = format.getExtension();
                    if (!path.endsWith(extension)) {
                        path += extension;
                    }
                }
                File f = new File(path);
                if (!f.exists()) {
                    break;
                }
                String msg = "The file '" + f.getName() + "' already exists.\n" + "Overwrite it?";
                if (DialogUtils.showConfirmWarning(msg, "Save work", false)) {
                    break;
                }
            } else {
                throw new OperationCancelledException("Save operation cancelled by user.");
            }
        }
        return path;
    }

}
