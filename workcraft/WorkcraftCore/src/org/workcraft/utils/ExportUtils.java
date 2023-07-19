package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.dom.Model;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Format;
import org.workcraft.interop.FormatFileFilter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ExportUtils {

    public static Importer chooseBestImporter(File file) {
        final PluginManager pm = Framework.getInstance().getPluginManager();
        for (Importer importer : pm.getSortedImporters()) {
            Format format = importer.getFormat();
            if (FormatFileFilter.checkFileFormat(file, format)) {
                return importer;
            }
        }
        return null;
    }

    public static Exporter chooseBestExporter(Model model, Format format) {
        return chooseBestExporter(model, format.getName(), format.getUuid());
    }

    public static Exporter chooseBestExporter(Model model, String formatName, UUID formatUuid) {
        final PluginManager pm = Framework.getInstance().getPluginManager();
        for (Exporter exporter : pm.getSortedExporters()) {
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

    public static void exportToFile(Model model, File file, Format format)
            throws IOException, SerialisationException {

        Exporter exporter = chooseBestExporter(model, format);
        if (exporter == null) {
            throw new NoExporterException(model.getDisplayName(), format.getName());
        }
        exportToFile(exporter, model, file);
    }

    public static void exportToFile(Exporter exporter, Model model, File file)
            throws IOException, SerialisationException {

        file.createNewFile();
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
            exporter.exportToFile(model, file);
            success = true;
        } finally {
            if (!success) {
                file.delete();
            }
        }
    }

    public static String getTitleAsIdentifier(String title) {
        // Non-empty module name is required.
        if ((title == null) || title.isEmpty()) {
            title = "Untitled";
        }
        // If the title start with a number, then prepend it with an underscore.
        if (Character.isDigit(title.charAt(0))) {
            title = "_" + title;
        }
        // Replace spaces and special symbols with underscores.
        return title.replaceAll("[^A-Za-z0-9_]", "_");
    }

    public static String getExportMessage(Model model, File file) {
        return getExportMessage(model, file, "Exporting model");
    }

    public static String getExportMessage(Model model, File file, String description) {
        String result = description;
        String title = model.getTitle();
        if (!title.isEmpty()) {
            result += " '" + title + "'";
        }
        result += " to file '" + file.getAbsolutePath() + "'.";
        return result;
    }

    public static String getExportHeader(String text, String linePrefix, String design) {
        return getExportHeader(text, linePrefix, design, null, null);
    }

    public static String getExportHeader(String text, String linePrefix, String design,
            File file, Format format) {

        EditorCommonSettings.ExportHeaderStyle headerStyle = EditorCommonSettings.getExportHeaderStyle();
        if ((headerStyle == null) || (headerStyle == EditorCommonSettings.ExportHeaderStyle.NONE)) {
            return "";
        }
        String result = linePrefix + " " + text + " generated by " + Info.getTitle();
        if (headerStyle == EditorCommonSettings.ExportHeaderStyle.DETAILED) {
            result += '\n' + linePrefix + " * Workcraft version:  " + Info.getVersionAndEdition();
            result += '\n' + linePrefix + " * Operating system:   " + getOperatingSystemDescription();
            result += '\n' + linePrefix + " * Creation timestamp: " + TextUtils.getCurrentTimestamp();
            if ((design != null) && !design.isEmpty()) {
                result += '\n' + linePrefix + " * Design title:       " + design;
            }
            if (format != null) {
                String fileName = file == null ? "<FILE_NAME>" : ("'" + file.getName() + "'");
                String command = "framework.exportWork(workspaceEntry, " + fileName + ", '" + format.getName() + "')";
                result += '\n' + linePrefix + " * JavaScript command: " + command;
            }
        }
        result += '\n';
        return result;
    }

    private static String getOperatingSystemDescription() {
        StringBuilder result = new StringBuilder();
        String osName = System.getProperty("os.name");
        if ((osName != null) && !osName.isEmpty()) {
            result.append(osName);
            String osArch = System.getProperty("os.arch");
            if ((osArch != null) && !osArch.isEmpty()) {
                result.append(" ").append(osArch);
            }
        }
        return result.toString();
    }

}
