// Standard function shortcuts

importPackage(java.lang);


// Printing

function print(msg) {
	System.out.print(msg);
}

function println(msg) {
	System.out.println(msg);
}

function printErr(msg) {
	System.err.print(msg);
}

function printlnErr(msg) {
	System.err.println(msg);
}


// Work file operations

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
	return framework.exportWork(work, path, format);
}


// Command execution

function runCommand(work, commandName) {
	framework.runCommand(work, commandName);
}

function executeCommand(work, commandName) {
	return framework.executeCommand(work, commandName);
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

function stopGUI() {
	framework.shutdownGUI();
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
