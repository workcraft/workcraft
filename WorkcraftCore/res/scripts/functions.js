// Standard function shortcuts
importPackage(java.lang);


// Printing

function print(msg) {
	System.out.print(msg);
}

function printErr(msg) {
	System.err.print(msg);
}

// Model file operations

function loadModel(path) {
	return framework.loadModel(path);
}

function saveModel(model, path) {
	framework.saveModel(model, path);
}

function importModel(path) {
	return framework.importFile(path);
}

function exportModel(model, path, format) {
	return framework.exportFile(model, path, format);
}

// Tool execution

function runCommand(model, commandName) {
	print(model + "\n");
	print(commandName + "\n");
	framework.runCommand(model, commandName);
}

// Script execution

function execResource(x) {
	framework.execJavaScriptResource(x);
}

function execFile(x) {
	framework.execJavaScriptFile(x);
}

// Configuration

function setConfigVar(k, v) {
	framework.setConfigVar(k, v);
}

function getConfigVar(k) {
	return framework.getConfigVar(k);
}

function saveConfig() {
	framework.saveConfig();
}

function loadConfig() {
	framework.loadConfig();
}

// GUI

function startGUI() {
	framework.startGUI();
}

function shutdownGUI() {
	framework.shutdownGUI();
}

// Exit

function quit() {
	framework.shutdown();
}

function exit() {
	framework.shutdown();
}

function shutdown() {
	framework.shutdown();
}
