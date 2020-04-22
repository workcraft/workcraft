// Workspace operations

framework.addJavaScriptHelp("getWorks", "",
    "return an iterable array of loaded works");

function getWorks() {
    return framework.getWorkspace().getWorks().toArray();
}


framework.addJavaScriptHelp("getWorkFile", "work",
    "return a file object for the model 'work'");

function getWorkFile(work) {
    return work.getFile();
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
