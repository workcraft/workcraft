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
		result.addAll(getMemTransitions());
		result.addAll(getHeadTransitions());
		result.addAll(getTailTransitions());
		return result;
	}

	public List<VisualSignalTransition> getMemTransitions() {
		List<VisualSignalTransition> result = new ArrayList<>();
		for (SignalStg mem: memList) {
			result.addAll(mem.getAllTransitions());
		}
		return result;
	}

	public List<VisualSignalTransition> getHeadTransitions() {
		List<VisualSignalTransition> result = new ArrayList<>();
		for (SignalStg head: headList) {
			result.addAll(head.getAllTransitions());
		}
		return result;
	}

	public List<VisualSignalTransition> getTailTransitions() {
		List<VisualSignalTransition> result = new ArrayList<>();
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
		result.addAll(getMemPlaces());
		result.addAll(getHeadPlaces());
		result.addAll(getTailPlaces());
		return result;
	}

	public List<VisualPlace> getMemPlaces() {
		List<VisualPlace> result = new ArrayList<>();
		for (SignalStg mem: memList) {
			result.addAll(mem.getAllPlaces());
		}
		return result;
	}

	public List<VisualPlace> getHeadPlaces() {
		List<VisualPlace> result = new ArrayList<>();
		for (SignalStg head: headList) {
			result.addAll(head.getAllPlaces());
		}
		return result;
	}

	public List<VisualPlace> getTailPlaces() {
		List<VisualPlace> result = new ArrayList<>();
		for (SignalStg tail: tailList) {
			result.addAll(tail.getAllPlaces());
		}
		return result;
	}

}
