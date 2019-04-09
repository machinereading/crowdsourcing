package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class GSParagraph {
	
	public static String getEString(String input) {
		String result = input;
		
		while(result.contains("<link>")) {
			int startIdx = result.indexOf("<link>");
			int barIdx = result.indexOf("|", startIdx);
			int endIdx = result.indexOf("</link>", barIdx);
			
			String entity = result.substring(barIdx + 1, endIdx);
			String former = result.substring(0, startIdx);
			String later = result.substring(endIdx + 7, result.length());
			
			result = former + "[" + entity + "]" + later;
		}
		
		while(result.contains("<elu>")) {
			int startIdx = result.indexOf("<elu>");
			int barIdx = result.indexOf("|", startIdx);
			int endIdx = result.indexOf("</elu>", barIdx);
			
			String entity = null;
			if(barIdx - startIdx < 8) {
				entity = result.substring(startIdx + 5, barIdx);
			} else {
				entity = result.substring(barIdx + 1, endIdx);
			}
			
			String former = result.substring(0, startIdx);
			String later = result.substring(endIdx + 6, result.length());
			
			if(barIdx - startIdx < 8) {
				result = former + entity + later;
			} else {
				result = former + "<" + entity + ">" + later;
			}
			
		}
		
		return result;
	}
	
	public static void main(String[] ar) throws Exception {

//		RestCaller rc = new RestCaller();
//
//		BufferedReader br = Files.newBufferedReader(Paths.get("data/gs/gs_label_par.id"));
//		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/gs/gs_label_par.txt"));
//		String input = null;
//		while ((input = br.readLine()) != null) {
//			StringTokenizer st = new StringTokenizer(input, "\t");
//			int docID = Integer.parseInt(st.nextToken());
//			int parID = Integer.parseInt(st.nextToken());
//
//			String jOut = rc.getParagraphFromLBox(docID, parID);
//
//			JSONParser parser = new JSONParser();
//			JSONObject item = (JSONObject) parser.parse(jOut);
//			JSONArray text = (JSONArray) item.get("text");
//			JSONArray SIDs = (JSONArray) item.get("global_s_ids");
//			
//			String out = "";
//			
//			Iterator<?> it = text.iterator();
//			while (it.hasNext()) {
//				String t = it.next().toString().trim();
//				if (!t.endsWith(".")) {
//					t = t + ".";
//				}
//				out += getEString(t) + " ";
//			}
//			System.out.println(out.trim() + "\t" + docID + "\t" + parID + "\t" + SIDs.toString());
//			bw.write(out.trim() + "\t" + docID + "\t" + parID + "\t" + SIDs.toString() + "\n");
//
//		}
//		
//		bw.close();

	}
}
