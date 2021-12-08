package org.workcraft.plugins.mpsat_verification.projection;

import org.workcraft.traces.Trace;

import java.util.HashMap;

public class Enabledness extends HashMap<String, Trace> {
    // This is a mapping of final event (transition without instance) of a Continuation to its preceding trace, e.g.:
    // Continuation: [a+, b-/1, c+/2]
    // Enabledness: c+ -> [a+, b-/1]
}
