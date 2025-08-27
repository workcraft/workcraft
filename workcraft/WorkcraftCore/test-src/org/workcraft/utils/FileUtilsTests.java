package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class FileUtilsTests {

    @Test
    void testFileNameWithoutExtension() {
        Assertions.assertEquals("",
                FileUtils.getFileNameWithoutExtension(new File("")));

        Assertions.assertEquals("name.mid",
                FileUtils.getFileNameWithoutExtension(new File("name.mid.ext"), false));

        Assertions.assertEquals("name",
                FileUtils.getFileNameWithoutExtension(new File("name.mid.ext"), true));

        Assertions.assertEquals("name.mid",
                FileUtils.getFileNameWithoutExtension(new File("/path/to/dir", "name.mid.ext"), false));

        Assertions.assertEquals("name",
                FileUtils.getFileNameWithoutExtension(new File("/path/to/dir/name.mid.ext"), true));
    }

    @Test
    void testFileExtension() {
        Assertions.assertEquals("",
                FileUtils.getFileExtension(new File(""), false));

        Assertions.assertEquals("",
                FileUtils.getFileExtension(new File("name"), false));

        Assertions.assertEquals(".ext",
                FileUtils.getFileExtension(new File("name.ext"), false));

        Assertions.assertEquals(".ext",
                FileUtils.getFileExtension(new File("/path/to/dir", "name.mid.ext"), false));

        Assertions.assertEquals(".mid.ext",
                FileUtils.getFileExtension(new File("path/to/dir/name.mid.ext"), true));
    }

    @Test
    void testTempPrefix() {
        Assertions.assertEquals("workcraft-",
                FileUtils.getTempPrefix(null));

        Assertions.assertEquals("workcraft-",
                FileUtils.getTempPrefix(""));

        Assertions.assertEquals("workcraft-abc-",
                FileUtils.getTempPrefix("abc"));

        Assertions.assertEquals("workcraft-a_b.c-",
                FileUtils.getTempPrefix("a b.c"));

        Assertions.assertEquals("workcraft-a_b_c-",
                FileUtils.getTempPrefix("a\tb\nc"));
    }

    @Test
    void testAllText() throws IOException {
        testAllText(null);
        testAllText(FileUtils.createTempDirectory("test"));
    }

    private void testAllText(File dir) throws IOException {
        File file = FileUtils.createTempFile("test", ".txt", dir);
        String text = " \nabc\n123\n\n";
        FileUtils.writeAllText(file, text);
        Assertions.assertEquals(text, FileUtils.readAllText(file));
    }

    @Test
    void testReadHeader() throws IOException {
        testReadHeader(" \nabc\n123\n\n", 5);
        testReadHeader("abc\n123\n", 10);
    }

    private void testReadHeader(String text, int count) throws IOException {
        File file = FileUtils.createTempFile("test", ".txt");
        FileUtils.writeAllText(file, text);
        int endIndex = Math.min(count, text.length());
        Assertions.assertEquals(text.substring(0, endIndex), FileUtils.readHeaderUtf8(file, count));
    }

    @Test
    void testContainsKeyword() throws IOException {
        File emptyFile = FileUtils.createTempFile("empty", ".txt");
        Assertions.assertTrue(FileUtils.containsKeyword(emptyFile, ""));
        Assertions.assertFalse(FileUtils.containsKeyword(emptyFile, " "));

        File file = FileUtils.createTempFile("test", ".txt");
        String text = " \nabc\n123 \n\neof";
        FileUtils.writeAllText(file, text);
        Assertions.assertTrue(FileUtils.containsKeyword(file, ""));
        Assertions.assertTrue(FileUtils.containsKeyword(file, "abc"));
        Assertions.assertTrue(FileUtils.containsKeyword(file, "12"));
        Assertions.assertFalse(FileUtils.containsKeyword(file, "key1"));
        Assertions.assertFalse(FileUtils.containsKeyword(file, "c\n1"));
        Assertions.assertTrue(FileUtils.containsKeyword(file, "eof"));
    }

    @Test
    void testFilePath() {
        String dirPath = System.getProperty("user.dir");
        File dir = new File(dirPath);
        String fileName = "test.txt";
        File file = new File(fileName);

        Assertions.assertEquals(FileUtils.appendFileSeparator(dirPath, File.separator) + fileName,
                FileUtils.getFullPath(file));

        Assertions.assertEquals(dirPath,
                FileUtils.getBasePath(file));

        Assertions.assertEquals(FileUtils.appendUnixFileSeparator(dir.getName()) + fileName,
                FileUtils.stripBase(file.getAbsolutePath(), FileUtils.getBasePath(dir)));

        Assertions.assertEquals(fileName,
                FileUtils.stripBase(file.getAbsolutePath(), dirPath));
    }

    @Test
    void testCommonRelativePath() {
        Assertions.assertNull(FileUtils.getUnixRelativePath(null, null));

        Assertions.assertNull(FileUtils.getUnixRelativePath(null, "dir"));

        Assertions.assertEquals("file.txt",
                FileUtils.getUnixRelativePath("file.txt", null));
    }

    @Test
    void testUnixRelativePath() {
        Assertions.assertEquals("/path/to/dir/file.txt",
                FileUtils.getUnixRelativePath("/path/to/dir/file.txt", null));

        String base = "/path/to/dir";

        Assertions.assertEquals("file.txt",
                FileUtils.getUnixRelativePath("/path/to/dir/file.txt", base));

        Assertions.assertEquals("../file.txt",
                FileUtils.getUnixRelativePath("/path/to/file.txt", base));

        Assertions.assertEquals("../another/dir/file.txt",
                FileUtils.getUnixRelativePath("/path/to/another/dir/file.txt", base));

        Assertions.assertEquals("../file.txt",
                FileUtils.getUnixRelativePath("/path/to/dir/../file.txt", base));

        Assertions.assertEquals("../../../another/dir/file.txt",
                FileUtils.getUnixRelativePath("/another/dir/file.txt", base));

        Assertions.assertEquals("file.txt",
                FileUtils.getUnixRelativePath("file.txt", base));
    }

    @Test
    void testWindowsRelativePath() {
        Assertions.assertEquals("C:/path/to/dir/file.txt",
                FileUtils.getUnixRelativePath("C:\\path\\to\\dir\\file.txt", null));

        String base = "C:\\path\\to\\dir";

        Assertions.assertEquals("file.txt",
                FileUtils.getUnixRelativePath("C:\\path\\to\\dir\\file.txt", base));

        Assertions.assertEquals("../file.txt",
                FileUtils.getUnixRelativePath("C:\\path\\to\\file.txt", base));

        Assertions.assertEquals("../another/dir/file.txt",
                FileUtils.getUnixRelativePath("C:\\path\\to\\another\\dir\\file.txt", base));

        Assertions.assertEquals("../file.txt",
                FileUtils.getUnixRelativePath("C:\\path\\to\\dir\\..\\file.txt", base));

        Assertions.assertEquals("../../../another/dir/file.txt",
                FileUtils.getUnixRelativePath("C:\\another\\dir\\file.txt", base));

        Assertions.assertEquals("file.txt",
                FileUtils.getUnixRelativePath("file.txt", base));

        Assertions.assertEquals("D:/another/dir/file.txt",
                FileUtils.getUnixRelativePath("D:\\another\\dir\\file.txt", base));
    }

}
