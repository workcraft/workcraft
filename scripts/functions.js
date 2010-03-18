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

function exec(x) {
	framework.execFile(x);
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

function setcvar(k,v) {
	framework.setConfigVar(k,v);
}

function getcvar(k) {
	return framework.getConfigVar(k);
}

function saveconfig() {
	framework.saveConfig("config/config.xml");
}

function loadconfig() {
	framework.loadConfig("config/config.xml");
}

function startgui() {
	framework.startGUI();
}

function shutdowngui() {
	framework.shutdownGUI();
}
