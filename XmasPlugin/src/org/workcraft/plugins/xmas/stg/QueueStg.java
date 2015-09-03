package org.workcraft.plugins.xmas.stg;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class QueueStg extends NodeStg {
	public final SignalStg i;
	public final SignalStg o;
	public final ArrayList<SignalStg> memList = new ArrayList<>();
	public final ArrayList<SignalStg> headList = new ArrayList<>();
	public final ArrayList<SignalStg> tailList = new ArrayList<>();

	public QueueStg(SignalStg i, SignalStg o, ArrayList<SignalStg> memList, ArrayList<SignalStg> headList, ArrayList<SignalStg> tailList) {
		this.i = i;
		this.o = o;
		this.memList.addAll(memList);
		this.headList.addAll(headList);
		this.tailList.addAll(tailList);
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		List<VisualSignalTransition> result = new ArrayList<>();
		result.addAll(i.getAllTransitions());
		result.addAll(o.getAllTransitions());
		for (SignalStg mem: memList) {
			result.addAll(mem.getAllTransitions());
		}
		for (SignalStg head: headList) {
			result.addAll(head.getAllTransitions());
		}
		for (SignalStg tail: tailList) {
			result.addAll(tail.getAllTransitions());
		}
		return result;
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		List<VisualPlace> result = new ArrayList<>();
		result.addAll(i.getAllPlaces());
		result.addAll(o.getAllPlaces());
		for (SignalStg memSignal: memList) {
			result.addAll(memSignal.getAllPlaces());
		}
		for (SignalStg hdSignal: headList) {
			result.addAll(hdSignal.getAllPlaces());
		}
		for (SignalStg tlSignal: tailList) {
			result.addAll(tlSignal.getAllPlaces());
		}
		return result;
	}

}
