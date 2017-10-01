// Configuration

framework.addJavaScriptHelp("setConfigVar", "set the config variable 'key' to value 'val'");

function setConfigVar(key, val) {
    framework.setConfigVar(key, val);
}


framework.addJavaScriptHelp("getConfigVar", "return the value of config variable 'key'");

function getConfigVar(key) {
    return framework.getConfigVar(key);
}


framework.addJavaScriptHelp("saveConfig", "save settings into the default config file");

function saveConfig() {
    framework.saveConfig();
}


framework.addJavaScriptHelp("loadConfig", "load settings from the default config file");

function loadConfig() {
    framework.loadConfig();
}
