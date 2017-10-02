// Execution of scriptable Workcraft commands and JavaScript scripts

framework.addJavaScriptHelp("execFile", "fileName",
    "execute JavaScript file 'path'");

function execFile(fileName) {
    framework.execJavaScriptFile(fileName);
}


framework.addJavaScriptHelp("runCommand", "work, commandName",
    "apply the command 'className' to the specified 'work' as a background task");

function runCommand(work, commandName) {
    framework.runCommand(work, commandName);
}


framework.addJavaScriptHelp("executeCommand", "work, commandName",
    "apply the command 'className' to the specified 'work' and wait for the result");

function executeCommand(work, commandName) {
    return framework.executeCommand(work, commandName);
}
