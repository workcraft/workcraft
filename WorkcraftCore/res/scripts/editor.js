// Editor helper functions

importPackage(java.util);

function select () {
    var result = new LinkedList();
    for (var i = 0; i < arguments.length; i++) {
	result.add (visualModel.getNodeByReference(arguments[i]));
   }

 visualModel.select(result);
}
