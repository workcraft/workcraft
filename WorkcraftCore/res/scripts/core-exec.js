// Command execution

function execResource(name) {
    framework.execJavaScriptResource(name);
}

function execFile(path) {
    framework.execJavaScriptFile(path);
}

function runCommand(work, commandName) {
    framework.runCommand(work, commandName);
}

function executeCommand(work, commandName) {
    return framework.executeCommand(work, commandName);
}
