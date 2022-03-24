// Workspace operations

framework.addJavaScriptHelp("getWorks", "",
    "return an iterable array of loaded works");

function getWorks() {
    return framework.getWorkspace().getWorks().toArray();
}


framework.addJavaScriptHelp("getWorkFile", "work",
    "return a file object for the model 'work'");

function getWorkFile(work) {
    return framework.getWorkspace().getFile(work);
}


framework.addJavaScriptHelp("getModelDescriptor", "work",
    "return descriptor string for the model 'work'");

function getModelDescriptor(work) {
    return work.getModelEntry().getDescriptor().getDisplayName();
}


framework.addJavaScriptHelp("getModelTitle", "work",
    "return title string for the model 'work'");

function getModelTitle(work) {
    return work.getModelEntry().getModel().getTitle();
}


framework.addJavaScriptHelp("setModelTitle", "work, title",
    "set title of the model 'work' to the string 'title'");

function setModelTitle(work, title) {
    return work.getModelEntry().getModel().setTitle(title);
}


framework.addJavaScriptHelp("closeWork", "work",
    "close the model 'work'");

function closeWork(work) {
    return framework.closeWork(work);
}


framework.addJavaScriptHelp("closeAllWorks", "",
    "close all the open works");

function closeAllWorks() {
    return framework.closeAllWorks();
}


framework.addJavaScriptHelp("setWorkingDirectory", "path",
    "set 'path' as the working directory");

function setWorkingDirectory(path) {
    // Helper function to convert env to a File object
    function getPathAsFile(path) {
        if (path instanceof File) {
            return path;
        }
        if ((path instanceof String) || (typeof path === "string")) {
            return new File(path);
        }
        throw "Path must be specified as File or String";
    }

    framework.setWorkingDirectory(getPathAsFile(path));

}


framework.addJavaScriptHelp("getWorkingDirectory", "",
    "get the working directory");

function getWorkingDirectory() {
    return framework.getWorkingDirectory();
}
