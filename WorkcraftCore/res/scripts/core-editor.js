// Editor helper functions

function select() {
    var result = new java.utils.LinkedList();
    for (var i = 0; i < arguments.length; i++) {
        node = visualModel.getNodeByReference(arguments[i]);
        result.add(node);
    }
    visualModel.select(result);
}
