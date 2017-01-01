// File operations

function load(path) {
	return framework.loadWork(path);
}

function import(path) {
	return framework.loadWork(path);
}

function save(work, path) {
	framework.saveWork(work, path);
}

function export(work, path, format) {
	framework.exportWork(work, path, format);
}


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


// Configuration

function setConfigVar(var, val) {
	framework.setConfigVar(var, val);
}

function getConfigVar(var) {
	return framework.getConfigVar(var);
}

function saveConfig() {
	framework.saveConfig();
}

function loadConfig() {
	framework.loadConfig();
}


// Miscileneous

function print(msg) {
	java.lang.System.out.print(msg);
}

function println(msg) {
	java.lang.System.out.println(msg);
}

function printErr(msg) {
	java.lang.System.err.print(msg);
}

function printlnErr(msg) {
	java.lang.System.err.println(msg);
}

function startGUI() {
	framework.startGUI();
}

function stopGUI() {
	framework.shutdownGUI();
}

function shutdownGUI() {
	framework.shutdownGUI();
}

function quit() {
	framework.shutdown();
}

function exit() {
	framework.shutdown();
}

function shutdown() {
	framework.shutdown();
}
