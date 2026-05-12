let work = load("buf.circuit.work");
let inputPortRef = "i";
let driverPinRef = "U0.O";
let drivenPinRef = "U0.I";

let originalPinInitToOne = getCircuitDriverInitToOne(work, driverPinRef);
setCircuitDriverInitToOne(work, driverPinRef, !originalPinInitToOne);
let modifiedPinInitToOne = getCircuitDriverInitToOne(work, driverPinRef);

let originalPortInitToOne = getCircuitDriverInitToOne(work, inputPortRef);
setCircuitDriverInitToOne(work, inputPortRef, !originalPortInitToOne);
let modifiedPortInitToOne = getCircuitDriverInitToOne(work, inputPortRef);

let originalPinForcedInit = getCircuitDriverForcedInit(work, driverPinRef);
setCircuitDriverForcedInit(work, driverPinRef, !originalPinForcedInit);
let modifiedPinForcedInit = getCircuitDriverForcedInit(work, driverPinRef);

let originalPortForcedInit = getCircuitDriverForcedInit(work, inputPortRef);
setCircuitDriverForcedInit(work, inputPortRef, !originalPortForcedInit);
let modifiedPortForcedInit = getCircuitDriverForcedInit(work, inputPortRef);

let originalPinPathBreaker = getCircuitPinPathBreaker(work, drivenPinRef);
setCircuitPinPathBreaker(work, drivenPinRef, !originalPinPathBreaker);
let modifiedPinPathBreaker = getCircuitPinPathBreaker(work, drivenPinRef);

let port = getCircuitPort(work, inputPortRef);
let originalPortConstraints = getPortConstraints(work, inputPortRef);
constrainCircuitInputPortRiseOnly(work, inputPortRef);
let modifiedPortConstraints = getPortConstraints(work, inputPortRef);
constrainCircuitInputPortAny(work, inputPortRef);
let clearedPortConstraints = getPortConstraints(work, inputPortRef);

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
