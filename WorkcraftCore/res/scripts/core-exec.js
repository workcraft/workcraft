// Execution of scriptable Workcraft commands and JavaScript scripts

framework.addJavaScriptHelp("execFile", "execute JavaScript file 'path'");

function execFile(path) {
    framework.execJavaScriptFile(path);
}


framework.addJavaScriptHelp("runCommand", "apply the command 'className' to the specified 'work' as a background task");

function runCommand(work, commandName) {
    framework.runCommand(work, commandName);
}


framework.addJavaScriptHelp("executeCommand", "apply the command 'className' to the specified 'work' and wait for the result");

function executeCommand(work, commandName) {
    return framework.executeCommand(work, commandName);
}
