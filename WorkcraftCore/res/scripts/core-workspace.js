// Workspace operations

framework.addJavaScriptHelp("getWorks", "",
    "return an iterable array of loaded works");

function getWorks() {
    return framework.getWorkspace().getWorks().toArray();
}


framework.addJavaScriptHelp("getWorkFile", "work",
    "return a file object for the specified work");

function getWorkFile(work) {
    return work.getFile();
}


framework.addJavaScriptHelp("getModelDescriptor", "work",
    "return a model descriptor string for the specified work");

function getModelDescriptor(work) {
    return work.getModelEntry().getDescriptor().getDisplayName();
}


framework.addJavaScriptHelp("getModelTitle", "work",
    "return a model title string for the specified work");

function getModelTitle(work) {
    return work.getModelEntry().getModel().getTitle();
}


framework.addJavaScriptHelp("setModelTitle", "work, title",
    "set the model title of the specified work to the title string");

function setModelTitle(work, title) {
    return work.getModelEntry().getModel().setTitle(title);
}
