package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Tutorial {
	public static void main(String[] ar) throws Exception {

		Map<String, String> relDef = new HashMap<>();

		BufferedReader br = Files.newBufferedReader(Paths.get("data/gs/prd_def_113.txt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String rel = st.nextToken();
			String def = st.nextToken();
			relDef.put(rel, def);
		}

		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/gs/tutorial_question_answer.txt"));
		br = Files.newBufferedReader(Paths.get("data/gs/kowiki-20170701-kbox_initial-wikilink-tutorial-hand.txt"));

		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			String obj = st.nextToken();
			String rel = st.nextToken();
			String stc = st.nextToken();
			String ans = st.nextToken();

			stc = stc.replace("_sbj_", sbj);
			stc = stc.replace("_obj_", obj);
			stc = stc.replace(" [[ ", "[");
			stc = stc.replace(" ]] ", "]");
			stc = stc.trim();

			String def = relDef.get(rel);
			if (def == null) {
				continue;
			}
			String defNL = def;
			defNL = defNL.replace("항목 주제인", "항목 주제 (이)라는");
			defNL = defNL.replace("항목 주제", "[" + sbj + "]");
			defNL = defNL + "은(는) " + "[" + obj + "]" + "인가요?";

			bw.write(stc + "\t" + defNL + "\t" + ans + "\t" + sbj + "\t" + obj + "\t" + rel + "\n");

		}

		bw.close();
	}
}
