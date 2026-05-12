// Get circuit contact by its reference
function getCircuitContact(work, ref) {
    let circuit = work.getModelEntry().getMathModel();
    let contact = circuit.getNodeByReference(ref);
    if (!(contact instanceof org.workcraft.plugins.circuit.FunctionContact)) {
        throw "Circuit contact '" + ref + "' not found";
    }
    return contact;
}

// Get circuit port by its reference
function getCircuitPort(work, ref) {
    let contact = getCircuitContact(work, ref);
    if (!contact.isPort()) {
        throw "Contact '" + ref + "' is not a port";
    }
    return contact;
}


// Get circuit component pin by its reference
function getCircuitPin(work, ref) {
    let contact = getCircuitContact(work, ref);
    if (!contact.isPin()) {
        throw "Contact '" + ref + "' is not a component pin";
    }
    return contact;
}

// Get circuit driver contact (input port or output pin) by its reference
function getCircuitDriver(work, ref, value) {
    let driver = getCircuitContact(work, ref);
    if (!driver.isDriver()) {
        throw "Contact '" + ref + "' is not a driver";
    }
    return driver;
}

framework.addJavaScriptHelp("setCircuitDriverInitToOne", "work, ref, value",
    "set 'value' as Init to one attribute for driver 'ref' (input port or output pin) in Circuit 'work'");

function setCircuitDriverInitToOne(work, ref, value) {
    let driver = getCircuitDriver(work, ref);
    driver.setInitToOne(value);
}


framework.addJavaScriptHelp("getCircuitDriverInitToOne", "work, ref",
    "get Init to one attribute for driver 'ref' (input port or output pin) in Circuit 'work'");

function getCircuitDriverInitToOne(work, ref) {
    let driver = getCircuitDriver(work, ref);
    return driver.getInitToOne();
}


framework.addJavaScriptHelp("setCircuitDriverForcedInit", "work, ref, value",
    "set 'value' as Forced init attribute for driver 'ref' (input port or output pin) in Circuit 'work'");

function setCircuitDriverForcedInit(work, ref, value) {
    let driver = getCircuitDriver(work, ref);
    driver.setForcedInit(value);
}


framework.addJavaScriptHelp("getCircuitDriverForcedInit", "work, ref",
    "get Forced init attribute for driver 'ref' (input port or output pin) in Circuit 'work'");

function getCircuitDriverForcedInit(work, ref) {
    let driver = getCircuitDriver(work, ref);
    return driver.getForcedInit();
}


framework.addJavaScriptHelp("setCircuitPinPathBreaker", "work, ref, value",
    "set 'value' as Path breaker attribute for component pin 'ref' in Circuit 'work'");

function setCircuitPinPathBreaker(work, ref, value) {
    let pin = getCircuitPin(work, ref);
    pin.setPathBreaker(value);
}


framework.addJavaScriptHelp("getCircuitPinPathBreaker", "work, ref",
    "get Path breaker attribute for component pin 'ref' in Circuit 'work'");

function getCircuitPinPathBreaker(work, ref) {
    let pin = getCircuitPin(work, ref);
    return pin.getPathBreaker();
}


framework.addJavaScriptHelp("constrainCircuitInputPortRiseOnly", "work, ref",
    "constrain input port 'ref' in Circuit 'work' as rise only");

function constrainCircuitInputPortRiseOnly(work, ref) {
    let port = getCircuitPort(work, ref);
    if (!port.isInput()) {
        throw "Port '" + ref + "' is not an input and cannot be constrained";
    }
    port.setSetFunction(org.workcraft.formula.One.getInstance());
    port.setResetFunction(org.workcraft.formula.Zero.getInstance());
}


framework.addJavaScriptHelp("constrainCircuitInputPortFallOnly", "work, ref",
    "constrain input port 'ref' in Circuit 'work' as fall only");

function constrainCircuitInputPortFallOnly(work, ref) {
    let port = getCircuitPort(work, ref);
    if (!port.isInput()) {
        throw "Port '" + ref + "' is not an input and cannot be constrained";
    }
    port.setResetFunction(org.workcraft.formula.One.getInstance());
    port.setSetFunction(org.workcraft.formula.Zero.getInstance());
}


framework.addJavaScriptHelp("constrainCircuitInputPortAny", "work, ref",
    "clear constrains from input port 'ref' in Circuit 'work'");

function constrainCircuitInputPortAny(work, ref) {
    let port = getCircuitPort(work, ref);
    if (!port.isInput()) {
        throw "Port '" + ref + "' is not an input and cannot be constrained";
    }
    port.setResetFunction(null);
    port.setSetFunction(null);
}


framework.addJavaScriptHelp("getCircuitDriverSetFunction", "work, ref",
    "get set function of driver 'ref' in Circuit 'work'");

function getCircuitDriverSetFunction(work, ref) {
    let driver = getCircuitDriver(work, ref);
    return org.workcraft.formula.visitors.StringGenerator.toString(driver.getSetFunction());
}


framework.addJavaScriptHelp("getCircuitDriverResetFunction", "work, ref",
    "get reset function of driver 'ref' in Circuit 'work'");

function getCircuitDriverResetFunction(work, ref) {
    let driver = getCircuitDriver(work, ref);
    return org.workcraft.formula.visitors.StringGenerator.toString(driver.getResetFunction());
}
