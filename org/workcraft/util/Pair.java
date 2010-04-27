package org.workcraft.util;

public class Pair <T1, T2> {
	private T1 first;
	private T2 second;

	public Pair (T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	public T1 getFirst() {
		return first;
	}

	public T2 getSecond() {
		return second;
	}

	public static <T1,T2> Pair<T1, T2> of(T1 first, T2 second)
	{
		return new Pair<T1, T2>(first, second);
	}
}
