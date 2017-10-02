// Editor helper functions

framework.addJavaScriptHelp("select", "",
    "select the nodes passed as a list of references");

function select() {
    var result = new java.utils.LinkedList();
    for (var i = 0; i < arguments.length; i++) {
        node = visualModel.getNodeByReference(arguments[i]);
        result.add(node);
    }
    visualModel.select(result);
}
