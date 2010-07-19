// standard function shortcuts
function quit() {
	framework.shutdown();
}

function done() {
	framework.shutdown();

}

function exit() {
	framework.shutdown();
}	

function shutdown() {
	framework.shutdown();
}

function load(x) {
	return framework.load(x);
}

function execResource(x) {
	framework.execJSResource(x);
}

function execFile(x) {
	framework.execJSFile(x);
}

function save(x) {
	framework.save(x);

}

function print(x) {
	java.lang.System.out.print(x);
}

function println(x) {
	java.lang.System.out.println(x);
}

function printerr(x) {
	java.lang.System.err.print(x);
}

function printlnerr(x) {
	java.lang.System.err.println(x);
}

function setConfigVar(k,v) {
	framework.setConfigVar(k,v);
}

function getConfigVar(k) {
	return framework.getConfigVar(k);
}

function saveConfig() {
	framework.saveConfig("config/config.xml");
}

function loadConfig() {
	framework.loadConfig("config/config.xml");
}

function startGUI() {
	framework.startGUI();
}

function shutdownGUI() {
	framework.shutdownGUI();
}
