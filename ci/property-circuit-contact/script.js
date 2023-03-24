work = load("buf.circuit.work");
inputPortRef = "i";
driverPinRef = "U0.O";
drivenPinRef = "U0.I";

originalPinInitToOne = getCircuitDriverInitToOne(work, driverPinRef);
setCircuitDriverInitToOne(work, driverPinRef, !originalPinInitToOne);
modifiedPinInitToOne = getCircuitDriverInitToOne(work, driverPinRef);

originalPortInitToOne = getCircuitDriverInitToOne(work, inputPortRef);
setCircuitDriverInitToOne(work, inputPortRef, !originalPortInitToOne);
modifiedPortInitToOne = getCircuitDriverInitToOne(work, inputPortRef);

originalPinForcedInit = getCircuitDriverForcedInit(work, driverPinRef);
setCircuitDriverForcedInit(work, driverPinRef, !originalPinForcedInit);
modifiedPinForcedInit = getCircuitDriverForcedInit(work, driverPinRef);

originalPortForcedInit = getCircuitDriverForcedInit(work, inputPortRef);
setCircuitDriverForcedInit(work, inputPortRef, !originalPortForcedInit);
modifiedPortForcedInit = getCircuitDriverForcedInit(work, inputPortRef);

originalPinPathBreaker = getCircuitPinPathBreaker(work, drivenPinRef);
setCircuitPinPathBreaker(work, drivenPinRef, !originalPinPathBreaker);
modifiedPinPathBreaker = getCircuitPinPathBreaker(work, drivenPinRef);

port = getCircuitPort(work, inputPortRef);
originalPortConstraints = getPortConstraints(work, inputPortRef);
constrainCircuitInputPortRiseOnly(work, inputPortRef);
modifiedPortConstraints = getPortConstraints(work, inputPortRef);
constrainCircuitInputPortAny(work, inputPortRef);
clearedPortConstraints = getPortConstraints(work, inputPortRef);

write(
    "Original Init to one on pin: " + originalPinInitToOne + "\n" +
    "Modified Init to one on pin: " + modifiedPinInitToOne + "\n" +
    "Original Init to one on port: " + originalPortInitToOne + "\n" +
    "Modified Init to one on port: " + modifiedPortInitToOne + "\n" +
    "Original Forced init on pin: " + originalPinForcedInit + "\n" +
    "Modified Forced init on pin: " + modifiedPinForcedInit + "\n" +
    "Original Forced init on port: " + originalPortForcedInit + "\n" +
    "Modified Forced init on port: " + modifiedPortForcedInit + "\n" +
    "Original Path breaker on pin: " + originalPinPathBreaker + "\n" +
    "Modified Path breaker on pin: " + modifiedPinPathBreaker + "\n" +
    "Original constroints on port: " + originalPortConstraints + "\n" +
    "Modified constraints on port: " + modifiedPortConstraints + "\n" +
    "Cleared constraints on port: " + clearedPortConstraints + "\n",
    "result.txt");

exit();

function getPortConstraints(work, ref) {
    return getCircuitDriverSetFunction(work, ref) + " / " + getCircuitDriverResetFunction(work, ref);
}
