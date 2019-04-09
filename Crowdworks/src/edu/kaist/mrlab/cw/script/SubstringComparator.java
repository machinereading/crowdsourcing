package edu.kaist.mrlab.cw.script;

import java.util.Comparator;

public class SubstringComparator implements Comparator {
	public int compare(Object o1, Object o2) {
		String s1 = o1.toString().substring(o1.toString().lastIndexOf("/") + 1);
		String s2 = o2.toString().substring(o2.toString().lastIndexOf("/") + 1);
		return s1.compareTo(s2);
		// or, more concisely:
		// return o1.toString().substring(1).equals(o2.toString().substring(1));
	}
}