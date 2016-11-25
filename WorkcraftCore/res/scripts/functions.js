// Standard function shortcuts
importPackage(java.lang);


// Printing

function print(msg) {
	System.out.print(msg);
}

function println(msg) {
	System.out.println(msg);
}

function printerr(msg) {
	System.err.print(msg);
}

function printlnerr(msg) {
	System.err.println(msg);
}

// File operations

function load(path) {
	return framework.load(path);
}

function save(model, path) {
	framework.save(model, path);
}

function import(path) {
	return framework.importFile(path);
}

function export(model, path, format) {
	return framework.exportFile(model, path, format);
}

// Script execution

function execResource(x) {
	framework.execJSResource(x);
}

function execFile(x) {
	framework.execJSFile(x);
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
