// Text output

importPackage(java.lang);
importPackage(java.io);

function print(msg) {
    System.out.println(msg);
}

function eprint(msg) {
    System.err.println(msg);
}

function write(text, fileName) {
    if (arguments.length < 2) {
        System.out.print(text);
    } else {
        dir = framework.getWorkingDirectory();
        file = new File(dir, fileName);
        fileWriter = new FileWriter(file);
        bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(text);
        bufferedWriter.close();
    }
}
