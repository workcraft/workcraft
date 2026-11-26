// Configuration

framework.addJavaScriptHelp("setConfigVar", "key, val",
    "set the config variable 'key' to value 'val'");

function setConfigVar(key, val) {
    framework.setConfigVar(key, val, true);
}


framework.addJavaScriptHelp("getConfigVar", "key",
    "return the value of config variable 'key'");

function getConfigVar(key) {
    return framework.getConfigVar(key, true);
}


framework.addJavaScriptHelp("saveConfig", "",
    "save settings into the config file Workcraft was started with");

function saveConfig() {
    framework.saveConfig();
}


framework.addJavaScriptHelp("loadConfig", "",
    "reload settings from the config files Workcraft was started with");

function loadConfig() {
    framework.loadConfig();
}
