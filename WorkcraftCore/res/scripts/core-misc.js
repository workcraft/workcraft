// Misc operations

framework.addJavaScriptHelp("startGUI", "start GUI");

function startGUI() {
    framework.startGUI();
}


framework.addJavaScriptHelp("stoptGUI", "stop GUI and switch to console mode");

function stopGUI() {
    framework.shutdownGUI();
}


framework.addJavaScriptHelp("quit", "exit Workcraft");

function quit() {
    framework.shutdown();
}


framework.addJavaScriptHelp("exit", "exit Workcraft");

function exit() {
    framework.shutdown();
}
