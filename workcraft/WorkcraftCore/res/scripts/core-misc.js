// Misc operations

framework.addJavaScriptHelp("startGUI", "", "start GUI");

function startGUI() {
    framework.startGUI();
}


framework.addJavaScriptHelp("stoptGUI", "", "stop GUI and switch to console mode");

function stopGUI() {
    framework.shutdownGUI();
}


framework.addJavaScriptHelp("quit", "", "request Workcraft shutdown after script execution is complete");

function quit() {
    framework.shutdown();
}


framework.addJavaScriptHelp("exit", "", "request Workcraft shutdown after script execution is complete");

function exit() {
    framework.shutdown();
}
