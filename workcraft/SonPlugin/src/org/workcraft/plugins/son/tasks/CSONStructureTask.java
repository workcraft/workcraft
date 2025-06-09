package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.elements.ChannelPlace;

public class CSONStructureTask extends AbstractStructuralVerification {

    private final SON net;

    private final Collection<Node> relationErrors = new ArrayList<>();
    private final Collection<Path> cycleErrors = new ArrayList<>();
    private final Collection<ONGroup> groupErrors = new HashSet<>();

    private final CSONCycleAlg csonCycleAlg;

    private int errNumber = 0;
    private static final int warningNumber = 0;

    public CSONStructureTask(SON net) {
        super(net);
        this.net = net;

        csonCycleAlg = new CSONCycleAlg(net);
    }

    @Override
    public void task(Collection<ONGroup> groups) {
        infoMsg("-----------------Communication-SON Structure Verification-----------------");

        // group info
        infoMsg("Initialising selected groups and components...");
        ArrayList<Node> components = new ArrayList<>();
        for (ONGroup group : groups) {
            components.addAll(group.getComponents());
        }

        infoMsg("Selected Groups : " + net.toString(groups));

        ArrayList<ChannelPlace> relatedCPlaces = new ArrayList<>();
        relatedCPlaces.addAll(getRelationAlg().getRelatedChannelPlace(groups));
        components.addAll(relatedCPlaces);

        infoMsg("Channel Places = " + relatedCPlaces.size());

        if (relatedCPlaces.isEmpty()) {
            infoMsg("Task terminated: no communication abstractions in selected groups.");
            return;
        }

        // channel place relation
        infoMsg("Running component relation tasks...");
        Collection<ChannelPlace> task1 = cPlaceRelationTask(relatedCPlaces);
        Collection<ChannelPlace> task2 = cPlaceConTypeTask(relatedCPlaces);
        relationErrors.addAll(task1);
        relationErrors.addAll(task2);

        if (relationErrors.isEmpty()) {
            infoMsg("Valid channel place relation.");
        } else {
            errNumber += relationErrors.size();
            for (Node cPlace : task1) {
                errMsg("Invalid channel place relation (input/output size != 1).", cPlace);
            }

            for (Node cPlace : task2) {
                errMsg("Invalid communication types (inconsistent input and output connection types).", cPlace);
            }
        }

        infoMsg("Component relation tasks complete.");

        // global cycle detection
        infoMsg("Running cycle detection task...");
        cycleErrors.addAll(getCSONCycleAlg().cycleTask(components));

        if (cycleErrors.isEmpty()) {
            infoMsg("Communication-SON is cycle free");
        } else {
            errNumber++;
            errMsg("Communication-SON involves global cycle paths = " + cycleErrors.size() + ".");
            int i = 1;
            for (Path cycle : cycleErrors) {
                infoMsg("Cycle " + i + ": " + cycle.toString(net));
                i++;
            }
        }

        infoMsg("Cycle detection task complete.\n");

    }

    private Collection<ChannelPlace> cPlaceRelationTask(ArrayList<ChannelPlace> cPlaces) {
        ArrayList<ChannelPlace> result = new ArrayList<>();

        for (ChannelPlace cPlace : cPlaces) {
            if (net.getPostset(cPlace).size() != 1 || net.getPreset(cPlace).size() != 1) {
                result.add(cPlace);
            }
        }
        return result;
    }

    private Collection<ChannelPlace> cPlaceConTypeTask(ArrayList<ChannelPlace> cPlaces) {
        ArrayList<ChannelPlace> result = new ArrayList<>();

        for (ChannelPlace cPlace : cPlaces) {
            if (net.getSONConnectionTypes(cPlace).size() > 1) {
                result.add(cPlace);
            }
        }
        return result;
    }

    public CSONCycleAlg getCSONCycleAlg() {
        return csonCycleAlg;
    }

    @Override
    public Collection<String> getRelationErrors() {
        return getRelationErrorsSetRefs(relationErrors);
    }

    @Override
    public Collection<ArrayList<String>> getCycleErrors() {
        return getCycleErrorsSetRefs(cycleErrors);
    }

    @Override
    public Collection<String> getGroupErrors() {
        return getGroupErrorsSetRefs(groupErrors);
    }

    @Override
    public int getErrNumber() {
        return this.errNumber;
    }

    @Override
    public int getWarningNumber() {
        return this.warningNumber;
    }

}
