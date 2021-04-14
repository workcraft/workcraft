package org.workcraft.utils;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

    public static final String TEMP_DIRECTORY_PREFIX = "workcraft-";

    public static void copyFile(File inFile, File outFile) throws IOException {
        outFile.getParentFile().mkdirs();
        try (FileOutputStream os = new FileOutputStream(outFile)) {
            copyFileToStream(inFile, os);
        }
    }

    public static String getFileNameWithoutExtension(File file) {
        return getFileNameWithoutExtension(file, false);
    }

    public static String getFileNameWithoutExtension(File file, boolean compoundExtension) {
        String name = file.getName();
        int i = compoundExtension ? name.indexOf('.') : name.lastIndexOf('.');
        return i == -1 ? name : name.substring(0, i);
    }

    public static String getFileExtension(File file) {
        return getFileExtension(file, false);
    }

    public static String getFileExtension(File file, boolean compoundExtension) {
        String name = file.getName();
        int i = compoundExtension ? name.indexOf('.') : name.lastIndexOf('.');
        return i == -1 ? "" : name.substring(i);
    }

    public static void dumpString(File out, String string) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(string.getBytes());
        }
    }

    public static void copyFileToStream(File inFile, OutputStream os) throws IOException {
        try (FileInputStream is = new FileInputStream(inFile);
                FileChannel inChannel = is.getChannel();
                WritableByteChannel outChannel = Channels.newChannel(os)) {

            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }

    public static void copyStreamToFile(InputStream is, File outFile) throws IOException {
        Files.copy(is, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static String getTempPrefix(String title) {
        String prefix = TEMP_DIRECTORY_PREFIX;
        if ((title != null) && !title.isEmpty()) {
            prefix = TEMP_DIRECTORY_PREFIX + title + "-";
        }
        return getCorrectTempPrefix(prefix);
    }

    private static String getCorrectTempPrefix(String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        // Prefix must be without spaces (replace spaces with underscores).
        prefix = prefix.replaceAll("\\s", "_");
        // Prefix must be at least 3 symbols long (prepend short prefix with
        // underscores).
        while (prefix.length() < 3) {
            prefix = "_" + prefix;
        }
        return prefix;
    }

    public static File createTempFile(String prefix, String suffix) {
        return createTempFile(prefix, suffix, null);
    }

    public static File createTempFile(String prefix, String suffix, File directory) {
        File tempFile = null;
        prefix = getCorrectTempPrefix(prefix);
        String errorMessage = "Cannot create a temporary file with prefix '" + prefix + "'";
        if (suffix == null) {
            suffix = "";
        } else {
            errorMessage += " and suffix '" + suffix + "'";
        }

        if (directory == null) {
            try {
                tempFile = File.createTempFile(prefix, suffix);
            } catch (IOException e) {
                throw new RuntimeException(errorMessage + ".");
            }
        } else {
            try {
                tempFile = File.createTempFile(prefix, suffix, directory);
            } catch (IOException e) {
                throw new RuntimeException(errorMessage + " under '" + directory.getAbsolutePath() + "' path.");
            }
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static File createTempDirectory() {
        return createTempDirectory(getCorrectTempPrefix(null));
    }

    public static File createTempDirectory(String prefix) {
        File tempDir = null;
        String errorMessage = "Cannot create a temporary directory with prefix '" + prefix + "'.";
        try {
            tempDir = File.createTempFile(getCorrectTempPrefix(prefix), "");
        } catch (IOException e) {
            throw new RuntimeException(errorMessage);
        }
        tempDir.delete();
        if (!tempDir.mkdir()) {
            throw new RuntimeException(errorMessage);
        }
        tempDir.deleteOnExit();
        return tempDir;
    }

    public static void copyAll(File source, File targetDir) throws IOException {
        if (!targetDir.isDirectory()) {
            throw new RuntimeException("Cannot copy files to a file that is not a directory.");
        }
        File target = new File(targetDir, source.getName());

        if (source.isDirectory()) {
            if (!target.mkdir()) {
                throw new RuntimeException("Cannot create directory " + target.getAbsolutePath());
            }
            for (File f : source.listFiles()) {
                copyAll(f, target);
            }
        } else {
            copyFile(source, target);
        }
    }

    public static void writeAllText(File file, String source) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(source);
        }
    }

    /**
     * Reads all text from the file using the default charset.
     */
    public static String readAllText(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return readAllText(stream);
        }
    }

    /**
     * Reads all text from the stream using the default charset.
     * Does not close the stream.
     */
    public static String readAllText(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder result = new StringBuilder();
        while (true) {
            String s = reader.readLine();
            if (s == null) {
                return result.toString();
            }
            result.append(s);
            result.append('\n');
        }
    }

    /**
     * Reads first count characters from the file using UTF8 charset.
     */
    public static String readHeaderUtf8(File file, int count) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            char[] buf = new char[count];
            int len = in.read(buf, 0, count);
            return new String(buf, 0, len);
        }
    }

    public static void moveFile(File from, File to) throws IOException {
        copyFile(from, to);
        from.delete();
    }

    public static String readAllTextFromSystemResource(String path) throws IOException {
        try (InputStream stream = ClassLoader.getSystemResourceAsStream(path)) {
            return readAllText(stream);
        }
    }

    public static boolean containsKeyword(File file, String keyword) {
        try {
            return containsKeyword(new FileInputStream(file), keyword);
        } catch (FileNotFoundException e1) {
        }
        return false;
    }

    public static boolean containsKeyword(InputStream is, String keyword) {
        boolean result = false;
        try (Scanner scanner = new Scanner(is)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(keyword)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public static void deleteOnExitRecursively(File file) {
        if (file != null) {
            // Note that deleteOnExit() for a directory needs to be called BEFORE the deletion of its content.
            file.deleteOnExit();
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteOnExitRecursively(f);
                }
            }
        }
    }

    public static boolean checkAvailability(File file, String title, boolean showMessageDialog) {
        String msg = getAvailabilityMessage(file);
        if (msg == null) {
            return true;
        }
        if (title == null) {
            title = "File access error";
        }
        if (showMessageDialog) {
            DialogUtils.showError(msg, title);
        } else {
            LogUtils.logError(title + ": " + msg);
        }
        return false;
    }

    private static String getAvailabilityMessage(File file) {
        if (file == null) {
            return "The file name is undefined.";
        }
        if (!file.exists()) {
            return "The path '" + file.getPath() + "' does not exist.";
        }
        if (file.isDirectory()) {
            return "The path '" + file.getPath() + "' is not a file.";
        }
        if (!file.canRead()) {
            return "The file '" + file.getPath() + "' cannot be read.";
        }
        return null;
    }

    public static void openExternally(String fileName, String errorTitle) {
        File file = new File(fileName);
        if (checkAvailability(file, errorTitle, true)) {
            DesktopApi.open(file);
        }
    }

    public static String getFullPath(File file) {
        if (file != null) {
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
            }
        }
        return null;
    }

    public static String getBasePath(File file) {
        if (file != null) {
            try {
                return file.getCanonicalFile().getParent();
            } catch (IOException e) {
            }
        }
        return null;
    }

    public static String stripBase(String path, String base) {
        path = fixSeparator(path);
        if ((base != null) && (path != null) && path.startsWith(base)) {
            String result = path.substring(base.length());
            while (result.startsWith("/")) {
                result = result.substring(1);
            }
            return result;
        }
        return path;
    }

    public static String fixSeparator(String path) {
        return path == null ? null : path.replace("\\", "/");
    }

    public static File getFileByPathAndBase(String path, String base) {
        File result = null;
        if (path != null) {
            result = new File(path);
            if (!result.isAbsolute()) {
                result = new File(base, path);
            }
        }
        return result;
    }

    public static File getFileByAbsoluteOrRelativePath(String path, File directory) {
        File file = null;
        if (path != null) {
            file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(directory, path);
            }
        }
        return file;
    }

    public static List<File> getDirectoryFiles(File dir) {
        List<File> result = new ArrayList<>();
        if ((dir != null) && dir.isDirectory()) {
            File[] fileArray = dir.listFiles();
            if (fileArray != null) {
                for (File file : fileArray) {
                    if ((file != null) && file.isFile()) {
                        result.add(file);
                    }
                }
            }
        }
        return result;
    }

}
