// Component refinement
topCircuitWork = load("top.circuit.work");
componentRef = "delay";
originalComponentRefinement = getCircuitComponentRefinement(topCircuitWork, componentRef);

setCircuitComponentRefinement(topCircuitWork, componentRef, "delay.stg.work");
modifiedComponentRefinement = getCircuitComponentRefinement(topCircuitWork, componentRef);

// STG refinemnt
stgWork = load("delay.stg.work");
originalStgRefinement = getStgRefinement(stgWork);

setStgRefinement(stgWork, "delay.circuit.work");
modifiedStgRefinement = getStgRefinement(stgWork);

// Circuit environment
circuitWork = load("delay.circuit.work");
originalEnvironmentFile = getCircuitEnvironment(circuitWork);
originalEnvironment = (originalEnvironmentFile instanceof File) ? originalEnvironmentFile.getName() : null;

setCircuitEnvironment(circuitWork, "wrong.work");
modifiedAsStringEnvironment = getCircuitEnvironment(circuitWork).getName();

setCircuitEnvironment(circuitWork, stgWork);
modifiedAsWorkEnvironment = getCircuitEnvironment(circuitWork).getName();

setCircuitEnvironment(circuitWork, getWorkFile(stgWork));
modifiedAsFileEnvironment = getCircuitEnvironment(circuitWork).getName();


write(
    "Original component refinement: " + originalComponentRefinement + "\n" +
    "Modified component refinement: " + modifiedComponentRefinement + "\n" +
    "Original STG refinement: " + originalStgRefinement + "\n" +
    "Modified STG refinement: " + modifiedStgRefinement + "\n" +
    "Original environment: " + originalEnvironment + "\n" +
    "Modified environment (set as string): " + modifiedAsStringEnvironment + "\n" +
    "Modified environment (set as work): " + modifiedAsWorkEnvironment + "\n" +
    "Modified environment (set as file): " + modifiedAsFileEnvironment + "\n",
    "refinements.txt");

exit();
