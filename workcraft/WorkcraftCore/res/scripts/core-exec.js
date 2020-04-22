// Execution of scriptable Workcraft commands and JavaScript scripts

framework.addJavaScriptHelp("execFile", "fileName",
    "execute JavaScript file 'fileName'");

function execFile(fileName) {
    framework.execJavaScriptFile(fileName);
}


framework.addJavaScriptHelp("runCommand", "work, className",
    "apply the command 'className' to the specified 'work' as a background task");

function runCommand(work, className) {
    framework.runCommand(work, className);
}


framework.addJavaScriptHelp("executeCommand", "work, className",
    "apply the command 'className' to the specified 'work' and wait for the result");

function executeCommand(work, className) {
    return framework.executeCommand(work, className);
}


framework.addJavaScriptHelp("executeDataCommand", "work, className, data",
    "apply the command 'className' with parameters 'data' to the specified 'work' and wait for the result");

function executeDataCommand(work, className, data) {
    return framework.executeDataCommand(work, className, data);
}
