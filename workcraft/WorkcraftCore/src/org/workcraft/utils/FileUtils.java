package org.workcraft.utils;

import javax.swing.*;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

    private static final String UNIX_FILE_SEPARATOR = "/";
    private static final String WINDOWS_FILE_SEPARATOR = "\\";
    private static final String TEMP_DIRECTORY_PREFIX = "workcraft-";

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

    public static String getFileExtension(File file, boolean compoundExtension) {
        String name = file.getName();
        int i = compoundExtension ? name.indexOf('.') : name.lastIndexOf('.');
        return i == -1 ? "" : name.substring(i);
    }

    public static void dumpString(File out, String string) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(string.getBytes(StandardCharsets.UTF_8));
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
        // Prefix must be at least 3 symbols long (prepend short prefix with
        // underscores).
        StringBuilder prefixBuilder = new StringBuilder(prefix.replaceAll("\\s", "_"));
        while (prefixBuilder.length() < 3) {
            prefixBuilder.insert(0, "_");
        }
        prefix = prefixBuilder.toString();
        return prefix;
    }

    public static File createTempFile(String prefix, String suffix) {
        return createTempFile(prefix, suffix, null);
    }

    public static File createTempFile(String prefix, String suffix, File directory) {
        File tempFile;
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
        File tempDir;
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

        if (source.isFile()) {
            copyFile(source, target);
        } else {
            if (!target.mkdir()) {
                throw new RuntimeException("Cannot create directory " + target.getAbsolutePath());
            }
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file != null) {
                        copyAll(file, target);
                    }
                }
            }
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
        try (InputStream stream = Files.newInputStream(file.toPath())) {
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
        try (InputStream stream = Files.newInputStream(file.toPath())) {
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
            return false;
        }
    }

    public static boolean containsKeyword(InputStream is, String keyword) {
        boolean result = keyword.isEmpty();
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

    public static boolean isReadableFile(File file) {
        return (file != null) && file.exists() && !file.isDirectory() && file.canRead();
    }

    public static boolean checkFileReadability(File file) {
        return checkFileReadability(file, null);
    }

    public static boolean checkFileReadability(File file, String errorTitleOrNull) {
        String msg = null;
        if (file == null) {
            msg = "Path is undefined";
        } else if (!file.exists()) {
            msg = "Path does not exist '" + file.getPath() + "'";
        } else if (file.isDirectory()) {
            msg = "Path is a directory '" + file.getPath() + "'";
        } else if (!file.canRead()) {
            msg = "Not a readable file '" + file.getPath() + "'";
        }
        showAccessError(msg, errorTitleOrNull);
        return msg == null;
    }

    public static boolean checkFileWritability(File file) {
        return checkFileWritability(file, null);
    }

    public static boolean checkFileWritability(File file, String errorTitleOrNull) {
        String msg = null;
        if (file == null) {
            msg = "Path is undefined";
        } else if (file.isDirectory()) {
            msg = "Path is a directory '" + file.getPath() + "'";
        } else if (file.exists() && !file.canWrite()) {
            msg = "Not a writable file '" + file.getPath() + "'";
        }
        showAccessError(msg, errorTitleOrNull);
        return msg == null;
    }

    public static boolean checkDirectoryWritability(File directory, String errorTitleOrNull) {
        String msg = null;
        if (directory == null) {
            msg = "Path is undefined";
        } else if (!directory.exists()) {
            msg = "Directory does not exist '" + directory.getPath() + "'";
        } else if (!directory.isDirectory()) {
            msg = "Path is not a directory '" + directory.getPath() + "'";
        } else if (directory.exists() && !directory.canWrite()) {
            msg = "Not a writable directory '" + directory.getPath() + "'";
        }
        showAccessError(msg, errorTitleOrNull);
        return msg == null;
    }

    private static void showAccessError(String msg, String title) {
        if ((msg != null) && (title != null)) {
            LogUtils.logError(title + ": " + msg);
            DialogUtils.showMessage(msg, title, JOptionPane.ERROR_MESSAGE, false);
        }
    }

    public static void openExternally(String fileName, String errorTitle) {
        File file = new File(fileName);
        if (checkFileReadability(file, errorTitle)) {
            DesktopApi.open(file);
        }
    }

    public static String getFullPath(File file) {
        return file == null ? null : Paths.get(file.getAbsolutePath()).normalize().toString();
    }

    public static String getBasePath(File file) {
        return file == null ? null : Paths.get(file.getAbsolutePath()).normalize().getParent().toString();
    }

    public static String stripBase(String path, String base) {
        path = useUnixFileSeparator(path);
        base = appendUnixFileSeparator(useUnixFileSeparator(base));
        if ((base != null) && (path != null) && path.startsWith(base)) {
            return path.substring(base.length());
        }
        return path;
    }

    public static String getUnixRelativePath(String path, String base) {
        path = useUnixFileSeparator(path);
        base = appendUnixFileSeparator(useUnixFileSeparator(base));
        String root = base == null ? null : base.substring(0, base.indexOf(UNIX_FILE_SEPARATOR) + 1);
        if ((path != null) && (root != null) && path.startsWith(root)) {
            Path pathPath = Paths.get(path);
            Path basePath = Paths.get(base);
            Path relativePath = basePath.relativize(pathPath);
            return useUnixFileSeparator(relativePath.toString());
        }
        return path;
    }

    public static String useUnixFileSeparator(String path) {
        return path == null ? null : path.replace(WINDOWS_FILE_SEPARATOR, UNIX_FILE_SEPARATOR);
    }

    public static String appendUnixFileSeparator(String path) {
        return appendFileSeparator(path, UNIX_FILE_SEPARATOR);
    }

    public static String appendFileSeparator(String path, String separator) {
        return (path == null) || path.endsWith(separator) ? path : path + separator;
    }

    public static File getFileByPathAndBase(String path, String base) {
        if (path == null) {
            return null;
        }
        Path result = Paths.get(path);
        if (!result.isAbsolute() && (base != null)) {
            result = Paths.get(base, path);
        }
        return result.normalize().toFile();
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

    public static File getFileDirectory(File file) {
        return ((file == null) || file.isDirectory()) ? file : file.getParentFile();
    }

    public static long getModtimeOrZero(String path) {
        if (path != null) {
            File file = new File(path);
            if (isReadableFile(file)) {
                return file.lastModified();
            }
        }
        return 0;
    }

}
