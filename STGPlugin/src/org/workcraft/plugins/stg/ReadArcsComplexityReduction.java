package org.workcraft.plugins.stg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.dom.Connection;
import org.workcraft.plugins.petri.Place;


public class ReadArcsComplexityReduction {

	public static STG reduce (STG source) {
		STG net = new STG();
		try
		{
			// TODO: doesn't work with multiple instances of the same type

			HashMap<SignalTransition, SignalTransition> oldNewTran = new HashMap<SignalTransition, SignalTransition>();

			for(SignalTransition t : source.getSignalTransitions())
			{
				SignalTransition newt = net.createSignalTransition(t.getSignalName());
				newt.setDirection(t.getDirection());
				newt.setSignalType(t.getSignalType());
				oldNewTran.put(t, newt);
			}

			// for each place in the original STG ...
			for (Place p: source.getPlaces()) {
				// count incoming and outgoing connections for each transition
				HashMap<SignalTransition, Integer> ins = new HashMap<SignalTransition,Integer>();
				HashMap<SignalTransition, Integer> outs = new HashMap<SignalTransition,Integer>();

				for (Connection c: source.getConnections(p)) {
					if (p==c.getFirst())  {
						if (!outs.containsKey(c.getSecond()))
							outs.put((SignalTransition)c.getSecond(),0);
						outs.put((SignalTransition)c.getSecond(), outs.get((SignalTransition)c.getSecond())+1);
					}
					if (p==c.getSecond())  {
						if (!ins.containsKey(c.getFirst()))
							ins.put((SignalTransition)c.getFirst(),0);
						ins.put((SignalTransition)c.getFirst(), ins.get((SignalTransition)c.getFirst())+1);
					}
				}

				HashSet<SignalTransition> trans = new HashSet<SignalTransition>();
				trans.addAll(ins.keySet());
				trans.addAll(outs.keySet());

				HashSet<SignalTransition> readATrans = new HashSet<SignalTransition>();

				// determine transitions connected as read-arcs
				for (SignalTransition t: trans) {

					Integer inc = ins.get(t);
					Integer outc = outs.get(t);
					if (inc!=null&&outc!=null&&inc==1&&outc==1) {
						// this is a read arc
						readATrans.add(t);
					}
				}

				LinkedList<Connection> cons = new LinkedList<Connection>();

				// find all connections which are not read-arcs
				for (Connection c: source.getConnections(p)) {
					if (!readATrans.contains(c.getFirst())
						&&!readATrans.contains(c.getSecond()))  {
						cons.add(c);
					}
				}

				//
				if (readATrans.size()==0) {
					// if there are no read-arcs, simply copy all the connections
					Place newP = net.createPlace();
					for(Connection c: cons) {
						if (c.getFirst()==p)
							net.connect(newP, oldNewTran.get(c.getSecond()));
						if (c.getSecond()==p)
							net.connect(oldNewTran.get(c.getFirst()), newP);
					}
				} else {
					// for each read-arc create a place, connect it correspondingly
					for (SignalTransition t: readATrans) {
						Place newP = net.createPlace();
						newP.setTokens(p.getTokens());
						net.connect(newP, oldNewTran.get(t));
						net.connect(oldNewTran.get(t), newP);
						for(Connection c: cons) {
							if (c.getFirst()==p)
								net.connect(newP, oldNewTran.get(c.getSecond()));
							if (c.getSecond()==p)
								net.connect(oldNewTran.get(c.getFirst()), newP);
						}

					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return net;
	}

}
