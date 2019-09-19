package org.workcraft.plugins;

import org.workcraft.Framework;
import org.workcraft.Info;
import org.workcraft.Version;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.plugins.builtin.settings.CommonDebugSettings;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompatibilityManager {

    private static final Pattern versionPattern = Pattern.compile("<" + Framework.META_VERSION_WORK_ELEMENT + " " +
            Framework.META_VERSION_MAJOR_WORK_ATTRIBUTE + "=\"([0-9]+)\" " +
            Framework.META_VERSION_MINOR_WORK_ATTRIBUTE + "=\"([0-9]+)\" " +
            Framework.META_VERSION_REVISION_WORK_ATTRIBUTE + "=\"([0-9]+)\" " +
            Framework.META_VERSION_STATUS_WORK_ATTRIBUTE + "=\"(.*)\"/>");

    private static final Pattern modelNamePattern = Pattern.compile("<model class=\"(.+?)\" ref=\"\">");

    private static final Pattern classNamePattern = Pattern.compile("<([A-Z]\\S*).*>");

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

    public void registerGlobalReplacement(Version version, String modelName, String pattern, String replacement) {
        ReplacementData replacementData = getReplacementData(version);
        Replacement replacementMap = replacementData.global.get(modelName);
        if (replacementMap == null) {
            replacementMap = new Replacement();
            replacementData.global.put(modelName, replacementMap);
        }
        replacementMap.put(pattern, replacement);
    }

    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public void registerContextualReplacement(Version version, String modelName, String className, String pattern, String replacement) {
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
        if (CommonDebugSettings.getVerboseCompatibilityManager()
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

    private Version extractVersion(String line) {
        Version result = null;
        Matcher matcher = versionPattern.matcher(line);
        if (matcher.find()) {
            String major = matcher.group(1);
            String minor = matcher.group(2);
            String revision = matcher.group(3);
            String status = matcher.group(4);
            result = new Version(major, minor, revision, status);
        }
        return result;
    }

    private Version extractVersion(InputStream is) throws IOException {
        Version result = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line = null;
        while ((result == null) && (line = reader.readLine()) != null) {
            result = extractVersion(line);
        }
        return result;
    }

    private String extractModelName(String line) {
        String result = null;
        Matcher matcher = modelNamePattern.matcher(line);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    private String extractClassName(String line) {
        String result = null;
        Matcher matcher = classNamePattern.matcher(line);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public ByteArrayInputStream process(File file) throws DeserialisationException, OperationCancelledException {
        ByteArrayInputStream result = null;
        try {
            ZipFile zipFile = new ZipFile(file, StandardCharsets.UTF_8);
            Version workVersion = null;
            ZipEntry metaZipEntry = zipFile.getEntry(Framework.META_WORK_ENTRY);
            if (metaZipEntry != null) {
                workVersion = extractVersion(zipFile.getInputStream(metaZipEntry));
            }
            Version currentVersion = Info.getVersion();
            if ((workVersion != null) && (currentVersion != null) && (currentVersion.compareTo(workVersion) < 0)) {
                String msg = "Workcraft v" + currentVersion + " may incorrectly read a file produced by newer Workcraft v" + workVersion;
                String msgFull = msg + ".\nProceed with loading of work file '" + file.getAbsolutePath() + "' anyway?";
                boolean proceed = DialogUtils.showConfirmWarning(msgFull, "Open file", true);
                if (!proceed) {
                    throw new OperationCancelledException(msg);
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

    public ByteArrayInputStream process(InputStream is, Version version) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8);
        ZipOutputStream zos = new ZipOutputStream(result, StandardCharsets.UTF_8);
        ZipEntry zei = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
        try {
            while ((zei = zis.getNextEntry()) != null) {
                ZipEntry zeo = new ZipEntry(zei.getName());
                boolean isMetaEntry = Framework.META_WORK_ENTRY.equals(zei.getName());
                zos.putNextEntry(zeo);
                String modelName = null;
                String className = null;
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (isMetaEntry) {
                        byte[] data = replaceMetaData(version, line).getBytes(StandardCharsets.UTF_8);
                        zos.write(data, 0, data.length);
                    } else if (modelName == null) {
                        String processedLine = replaceModelName(version, line);
                        byte[] data = processedLine.getBytes(StandardCharsets.UTF_8);
                        zos.write(data, 0, data.length);
                        modelName = extractModelName(processedLine);
                    } else {
                        String s = extractClassName(line);
                        if (s != null) {
                            className = s;
                        }
                        byte[] data = replaceEntry(version, modelName, className, line).getBytes(StandardCharsets.UTF_8);
                        zos.write(data, 0, data.length);
                    }
                }
                zis.closeEntry();
                zos.closeEntry();
            }
            zos.close();
            result.close();
        } catch (IOException e) {
        }
        return new ByteArrayInputStream(result.toByteArray());
    }

}
