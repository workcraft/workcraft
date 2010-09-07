package org.workcraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Trace extends ArrayList<String>{
	public String toString() {
		StringBuffer result = new StringBuffer("");

		boolean first = true;

		for (String t : this) {
			if (first)
				first = false;
			else
				result.append(',');
			result.append(t);
		}

		return result.toString();
	}

	public static void save (OutputStream os, Trace trace) throws IOException {
		os.write(trace.toString().getBytes());
	}

	public static Trace load (InputStream is, Trace trace) throws IOException {
		Trace result = new Trace();
		for (String s : new BufferedReader(new InputStreamReader(is)).readLine().split(","))
			result.add(s);
		return result;
	}
}
