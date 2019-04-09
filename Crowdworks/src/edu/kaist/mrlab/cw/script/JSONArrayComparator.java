package edu.kaist.mrlab.cw.script;

import java.util.Comparator;

import org.json.simple.JSONObject;

public class JSONArrayComparator implements Comparator<JSONObject> {

	@Override
	public int compare(JSONObject jsonObjectA, JSONObject jsonObjectB) {
		int compare = 0;
		try {
			int keyA = Integer.parseInt(jsonObjectA.get("startPosition").toString());
			int keyB = Integer.parseInt(jsonObjectB.get("startPosition").toString());
			compare = Integer.compare(keyA, keyB);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compare;
	}

}
