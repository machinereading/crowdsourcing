package edu.kaist.mrlab.cw.script;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cedarsoftware.util.io.JsonWriter;

public class RETutorial {
	
	Set<String> relSet = new HashSet<>();

	Map<String, String> relDef = new HashMap<>();

	public void loadRelDef() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/gs/prd_def_113.txt"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String rel = st.nextToken();
			String def = st.nextToken();
			relDef.put(rel, def);
		}
	}
	
	List<String> questionArr = new ArrayList<>();
	
	public void loadQuestions() throws Exception {
		Reader in = new FileReader("data/gs/manual_cleared_data_lbox.csv");
		Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			
			String sentence = record.get(0);
			String relation = record.get(1);
			
			questionArr.add(sentence + "\t" + relation + "\t" + "T");
		}
		
		in = new FileReader("data/gs/false_gold_standard_ver1.csv");
		records = CSVFormat.EXCEL.parse(in);
		for (CSVRecord record : records) {
			
			String sentence = record.get(0);
			String relation = record.get(1);
			
			questionArr.add(sentence + "\t" + relation + "\t" + "F");
		}
	}


	public void generate() throws Exception {
		
		long seed = System.nanoTime();
		Collections.shuffle(questionArr, new Random(seed));
		
		JSONObject result = new JSONObject();
		JSONArray paragraphs = new JSONArray();
		
		int fileCount = 0;
		int localCount = 0;

		BufferedWriter bw = null;
		
		for(String q : questionArr) {
			
			StringTokenizer st = new StringTokenizer(q, "\t");
			String sentence = st.nextToken();
			String relation = st.nextToken();
			String isTrue = st.nextToken();
			
			if(relSet.contains(relation + isTrue)) {
				continue;
			} else {
				relSet.add(relation + isTrue);
			}

			int e1SIdx = sentence.indexOf("<e1>");
			int e1EIdx = sentence.indexOf("</e1>");

			int e2SIdx = sentence.indexOf("<e2>");
			int e2EIdx = sentence.indexOf("</e2>");
			
			String sbj = sentence.substring(e1SIdx + 4, e1EIdx);
			String obj = sentence.substring(e2SIdx + 4, e2EIdx);

			String stc = sentence.replace("<e1>" + sbj + "</e1>", "[" + sbj + "]");
			stc = stc.replace("<e2>" + obj + "</e2>", "[" + obj + "]");
			
			if(e1SIdx < e2SIdx) {
				
				e1SIdx++;
				e1EIdx -= 3;
				e2SIdx -= 6;
				e2EIdx -= 10;
				
			} else {
				
				e2SIdx++;
				e2EIdx -= 3;
				e1SIdx -= 6;
				e1EIdx -= 10;
				
			}

			JSONArray questions = new JSONArray();
			JSONObject question = new JSONObject();

			String def = relDef.get(relation);
			String defNL = def;
			if (def == null) {
				defNL = "[" + sbj + "] 의 " + relation + "은 [" + obj+ "] 인가요?";
			} else {
				defNL = defNL.replace("항목 주제인", "항목 주제 (이)라는");
				defNL = defNL.replace("항목 주제", "[" + sbj + "]");
				defNL = defNL + "은(는) [" + obj + "] 인가요?" + " (" + relation + ")";
			}
			defNL = defNL.replace("항목 주제인", "항목 주제 (이)라는");
			defNL = defNL.replace("항목 주제", "[" + sbj + "]");
			defNL = defNL + "은(는) [" + obj + "] 인가요?" + " (" + relation + ")";

			question.put("ans", isTrue);
			question.put("ansArr", "");
			question.put("e1", sbj);
			question.put("e2", obj);
			question.put("qid", 0);
			question.put("rel", relation);
			question.put("nlq", defNL);
			
			question.put("e1St", e1SIdx);
			question.put("e1En", e1EIdx);
			question.put("e2St", e2SIdx);
			question.put("e2En", e2EIdx);

			questions.add(question);
			
			JSONObject paragraph = new JSONObject();
			paragraph.put("globalSId", "");
			paragraph.put("parID", "");
			paragraph.put("docID", "");
			paragraph.put("plainText", stc);
			paragraph.put("questions", questions);
			
			paragraphs.add(paragraph);
			
			localCount++;
			
			if(localCount > 20) {
				
				result.put("paragraphs", paragraphs);
				
				bw = Files.newBufferedWriter(Paths.get("data/gs/task4/task4_tutorial_" + fileCount + ".json"));
				bw.write(JsonWriter.formatJson(result.toString()));	
				bw.close();
				fileCount++;
				localCount = 0;
				result = new JSONObject();
				paragraphs.clear();
				
			}

		}

		
	}

	public static void main(String[] ar) throws Exception {

		RETutorial ret = new RETutorial();
		ret.loadRelDef();
		ret.loadQuestions();
		ret.generate();

	}
}
