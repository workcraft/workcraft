package org.workcraft.plugins.xmas.stg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.converters.NodeStg;

public class QueueStg extends NodeStg {
    public final ContactStg i;
    public final ContactStg o;
    public final ArrayList<SlotStg> slotList = new ArrayList<>();

    public QueueStg(ContactStg i, ContactStg o, ArrayList<SlotStg> slotList) {
        this.i = i;
        this.o = o;
        this.slotList.addAll(slotList);
    }

    @Override
    public List<VisualSignalTransition> getAllTransitions() {
        List<VisualSignalTransition> result = new ArrayList<>();
        result.addAll(i.getAllTransitions());
        result.addAll(o.getAllTransitions());
        result.addAll(getSlotTransitions());
        return result;
    }

    public List<VisualSignalTransition> getSlotTransitions() {
        List<VisualSignalTransition> result = new ArrayList<>();
        for (SlotStg slot: slotList) {
            result.addAll(slot.getAllTransitions());
        }
        return result;
    }

    @Override
    public List<VisualPlace> getAllPlaces() {
        List<VisualPlace> result = new ArrayList<>();
        result.addAll(i.getAllPlaces());
        result.addAll(o.getAllPlaces());
        result.addAll(getSlotPlaces());
        return result;
    }

    public List<VisualPlace> getSlotPlaces() {
        List<VisualPlace> result = new ArrayList<>();
        for (SlotStg slot: slotList) {
            result.addAll(slot.getAllPlaces());
        }
        return result;
    }

    public Collection<VisualSignalTransition> getMemTransitions() {
        List<VisualSignalTransition> result = new ArrayList<>();
        for (SlotStg slot: slotList) {
            result.addAll(slot.mem.getAllTransitions());
        }
        return result;
    }

    public Collection<VisualSignalTransition> getHeadTransitions() {
        List<VisualSignalTransition> result = new ArrayList<>();
        for (SlotStg slot: slotList) {
            result.addAll(slot.hd.getAllTransitions());
        }
        return result;
    }

    public Collection<VisualSignalTransition> getTailTransitions() {
        List<VisualSignalTransition> result = new ArrayList<>();
        for (SlotStg slot: slotList) {
            result.addAll(slot.tl.getAllTransitions());
        }
        return result;
    }

}
