package org.workcraft.plugins;

import org.workcraft.Info;
import org.workcraft.Version;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompatibilityManager {

    @SuppressWarnings("serial")
    private class Replacement extends HashMap<String, String> {
    }

    @SuppressWarnings("serial")
    private class ContextualReplacement extends HashMap<String, Replacement> {
    }

    @SuppressWarnings("serial")
    private class NestedContextualReplacement extends HashMap<String, ContextualReplacement> {
    }

    private class ReplacementData {
        private final Replacement meta = new Replacement();
        private final Replacement model = new Replacement();
        private final ContextualReplacement global = new ContextualReplacement();
        private final NestedContextualReplacement local = new NestedContextualReplacement();
    }

    @SuppressWarnings("serial")
    private class VersionedReplacementData extends TreeMap<Version, ReplacementData> {
    }

    private final VersionedReplacementData versionedReplacementData = new VersionedReplacementData();

    private ReplacementData getReplacementData(Version version) {
        ReplacementData result = versionedReplacementData.get(version);
        if (result == null) {
            result = new ReplacementData();
            versionedReplacementData.put(version, result);
        }
        return result;
    }

    private List<ReplacementData> getOrderedApplicableData(Version version) {
        List<ReplacementData> result = new ArrayList<>();
        for (Version sinceVersion : versionedReplacementData.keySet()) {
            if ((version == null) || (version.compareTo(sinceVersion) < 0)) {
                ReplacementData data = getReplacementData(sinceVersion);
                result.add(data);
            }
        }
        return result;
    }

    public void registerMetaReplacement(Version version, String oldMetaData, String metaData) {
        ReplacementData replacementData = getReplacementData(version);
        replacementData.meta.put(oldMetaData, metaData);
    }

    public void registerModelReplacement(Version version, String oldModelName, String modelName) {
        ReplacementData replacementData = getReplacementData(version);
        replacementData.model.put(oldModelName, modelName);
    }

    public void registerGlobalReplacement(Version version, String modelName, String pattern,
            String replacement) {

        ReplacementData replacementData = getReplacementData(version);
        Replacement replacementMap = replacementData.global.get(modelName);
        if (replacementMap == null) {
            replacementMap = new Replacement();
            replacementData.global.put(modelName, replacementMap);
        }
        replacementMap.put(pattern, replacement);
    }

    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public void registerContextualReplacement(Version version, String modelName, String className,
            String pattern, String replacement) {

        ReplacementData replacementData = getReplacementData(version);
        ContextualReplacement contextualMap = replacementData.local.get(modelName);
        if (contextualMap == null) {
            contextualMap = new ContextualReplacement();
            replacementData.local.put(modelName, contextualMap);
        }
        Replacement replacementMap = contextualMap.get(className);
        if (replacementMap == null) {
            replacementMap = new Replacement();
            contextualMap.put(className, replacementMap);
        }
        replacementMap.put(pattern, replacement);
    }

    private String replace(String line, Map.Entry<String, String> replacement, String message) {
        String newline = line.replaceAll(replacement.getKey(), replacement.getValue());
        if (DebugCommonSettings.getVerboseCompatibilityManager()
                && (message != null) && !line.equals(newline)) {
            LogUtils.logInfo("Compatibility management: " + message);
            LogUtils.logInfo("  old: " + line);
            LogUtils.logInfo("  new: " + newline);
        }
        return newline;
    }

    private String replaceMetaData(Version version, String line) {
        for (ReplacementData data : getOrderedApplicableData(version)) {
            for (Map.Entry<String, String> replacement : data.meta.entrySet()) {
                if (line.contains(replacement.getKey())) {
                    line = replace(line, replacement, "legacy meta data");
                }
            }
        }
        return line;
    }

    private String replaceModelName(Version version, String line) {
        for (ReplacementData data : getOrderedApplicableData(version)) {
            for (Map.Entry<String, String> replacement : data.model.entrySet()) {
                if (line.contains(replacement.getKey())) {
                    line = replace(line, replacement, "legacy model class");
                }
            }
        }
        return line;
    }

    private String replaceGlobalEntry(Version version, String modelName, String line) {
        for (ReplacementData data : getOrderedApplicableData(version)) {
            Replacement replacementMap = data.global.get(modelName);
            if (replacementMap != null) {
                for (Map.Entry<String, String> replacement : replacementMap.entrySet()) {
                    line = replace(line, replacement, "global replacement");
                }
            }
        }
        return line;
    }

    private String replaceContextualEntry(Version version, String modelName, String className, String line) {
        for (ReplacementData data : getOrderedApplicableData(version)) {
            HashMap<String, Replacement> contextualMap = data.local.get(modelName);
            if (contextualMap != null) {
                Replacement replacementMap = contextualMap.get(className);
                if (replacementMap != null) {
                    for (Map.Entry<String, String> replacement : replacementMap.entrySet()) {
                        line = replace(line, replacement, "contextual replacement for " + className);
                    }
                }
            }
        }
        return line;
    }

    private String replaceEntry(Version version, String modelName, String className, String line) {
        line = replaceGlobalEntry(version, modelName, line);
        line = replaceContextualEntry(version, modelName, className, line);
        return line;
    }

    public ByteArrayInputStream process(File file) throws DeserialisationException, OperationCancelledException {
        ByteArrayInputStream result = null;
        try {
            ZipFile zipFile = new ZipFile(file, StandardCharsets.UTF_8);
            Version workVersion = WorkUtils.extractVersion(zipFile);
            Version currentVersion = Info.getVersion();
            if ((workVersion != null) && (currentVersion != null) && (currentVersion.compareTo(workVersion) < 0)) {
                String msg = "Workcraft v" + currentVersion
                        + " may incorrectly read a file produced by newer Workcraft v" + workVersion;

                String msgFull = msg + ".\nProceed with loading of work file '" + file.getAbsolutePath() + "' anyway?";
                boolean proceed = DialogUtils.showConfirmWarning(msgFull, "Open file", true);
                if (!proceed) {
                    throw new OperationCancelledException();
                }
            }
            FileInputStream fis = new FileInputStream(file);
            result = process(fis, workVersion);
            zipFile.close();
        } catch (IOException e) {
            throw new DeserialisationException(e);
        }
        return result;
    }

    public ByteArrayInputStream process(InputStream is, Version version) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8);
        ZipOutputStream zos = new ZipOutputStream(result, StandardCharsets.UTF_8);
        ZipEntry zei = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
        while ((zei = zis.getNextEntry()) != null) {
            ZipEntry zeo = new ZipEntry(zei.getName());
            zos.putNextEntry(zeo);
            String modelName = null;
            String className = null;
            String line = null;
            while ((line = reader.readLine()) != null) {
                line += "\n";
                if (WorkUtils.isMetaEntry(zei)) {
                    zos.write(replaceMetaData(version, line).getBytes(StandardCharsets.UTF_8));
                } else if (modelName == null) {
                    String processedLine = replaceModelName(version, line);
                    zos.write(processedLine.getBytes(StandardCharsets.UTF_8));
                    modelName = WorkUtils.extractModelName(processedLine);
                } else {
                    String s = WorkUtils.extractClassName(line);
                    if (s != null) {
                        className = s;
                    }
                    zos.write(replaceEntry(version, modelName, className, line).getBytes(StandardCharsets.UTF_8));
                }
            }
            zis.closeEntry();
            zos.closeEntry();
        }
        zos.close();
        result.close();
        return new ByteArrayInputStream(result.toByteArray());
    }

}
