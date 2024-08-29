// Editor helper functions

framework.addJavaScriptHelp("select", "ref, ...",
    "select the nodes passed as a list of references");

function select() {
    var result = new java.util.LinkedList();
    for (var i = 0; i < arguments.length; i++) {
        node = visualModel.getVisualComponentByMathReference(arguments[i]);
        result.add(node);
    }
    visualModel.select(result);
}
