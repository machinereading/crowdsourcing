package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.cedarsoftware.util.io.JsonWriter;

import edu.kaist.mrlab.cw.data.Entity;

public class RE_from_Task2 {

	private static int paragraphCount = 0;
	private static int questionCount = 0;

	private Path inputPath;
	private List<Path> filePathList;
	
//	private List<String> prevFilePathList = new ArrayList<>();;
	
	public void setPrevSet() throws Exception {
		
		while ((inputPath = RE_from_Task2.this.extractInputPath()) != null) {
			String fileName = inputPath.getFileName().toString();
			if(fileName.contains("DS_Store")) {
				continue;
			}
//			prevFilePathList.add(fileName);
		}
		
	}

	public void loadCorpus() throws Exception {
		this.filePathList = Files.walk(this.inputPath).filter(p -> Files.isRegularFile(p))
				.collect(Collectors.toCollection(ArrayList::new));
		System.out.println("done!");
		System.out.println("Number of file paths: " + filePathList.size());
	}

	private synchronized Path extractInputPath() {
		if (this.filePathList.isEmpty()) {
			return null;
		} else {
			return this.filePathList.remove(this.filePathList.size() - 1);
		}
	}

	public RE_from_Task2 setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	HashMap<String, Set<String>> entityRelationMap = new HashMap<>();

	public void loadTriples() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/init.tsv"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String sbj = st.nextToken();
			String prd = st.nextToken();
			String obj = st.nextToken();
			String key = sbj + "\t" + obj;
			Set<String> values = null;
			if (entityRelationMap.containsKey(key)) {
				values = entityRelationMap.get(key);
			} else {
				values = new HashSet<String>();
			}
			values.add(prd);
			entityRelationMap.put(key, values);
		}
	}

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

	public void generate() throws Exception {

		int globalCount = 0;
		int fileCount = 1;

		JSONObject result = new JSONObject();
		JSONArray paragraphs = new JSONArray();
		
		int prevCount = 0;

		BufferedReader br = null;
		while ((inputPath = RE_from_Task2.this.extractInputPath()) != null) {

			String fileName = inputPath.getFileName().toString();

			if (fileName.contains("DS_Store")
//					|| prevFilePathList.contains(fileName)
					) {
				prevCount++;
				continue;
			}

			if (fileName.contains("700992_8")) {
				System.out.println();
			}

			int localCount = 0;

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			System.out.println("doing: " + inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			JSONObject paragraph = new JSONObject();

			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(str.toString().trim());

			int wikiPageID = Integer.parseInt(object.get("docID").toString());
			int pIdx = Integer.parseInt(object.get("parID").toString());
			String text = object.get("plainText").toString();
			String globalSId = object.get("globalSID").toString();
			JSONArray entities = (JSONArray) object.get("entities");
//			JSONArray ZAs = (JSONArray) object.get("result");
			int numOfEntity = entities.size();

			paragraph.put("globalSId", globalSId);
			paragraph.put("docID", wikiPageID);
			paragraph.put("parID", pIdx);
			paragraph.put("plainText", text);

			JSONArray questions = new JSONArray();

			String taggedText = "";
			String mentionText = "";

			List<Entity> entityList = new ArrayList<>();

			int begin = 0;

			Iterator<JSONObject> it = entities.iterator();
			while (it.hasNext()) {
				JSONObject item = it.next();
				int eIdx = Integer.parseInt(item.get("id").toString());
				int st = Integer.parseInt(item.get("st").toString());
				int en = Integer.parseInt(item.get("en").toString());
				String surface = item.get("surface").toString();
				String entityName = item.get("entityName").toString();
				String neType = item.get("ne_type").toString();
				String eType = item.get("eType").toString();
				String type = "";
				if (item.containsKey("type")) {
					type = item.get("type").toString();
				}

//				System.out.println(surface + "\t" + entityName + "\t" + begin + "\t" + st + "\t" + en);
				
				if (entityName.equals("")) {
					if (st >= begin) {
						mentionText += text.substring(begin, en);
						taggedText += text.substring(begin, en);
					}
					begin = en;
				} else {
					Entity entity = new Entity(eIdx, st, en, surface, entityName, eType);
					entityList.add(entity);

					if (st >= begin) {

						if (type.equals("PRONOUN")) {
							mentionText += text.substring(begin, st);
							mentionText += "[" + entityName.replace("_", " ") + "] ";
						} else {
							mentionText += text.substring(begin, st);
							mentionText += "[" + surface.replace("_", " ") + "] ";
						}

						taggedText += text.substring(begin, st);
						taggedText += "[" + entityName + "] ";
					}

					begin = en;

				}

			}

			taggedText += text.substring(begin, text.length());
			mentionText += text.substring(begin, text.length());

//			System.out.println(taggedText);

			paragraph.put("taggedText", taggedText);
			paragraph.put("mentionText", mentionText);

			Set<String> defNLSet = new HashSet<>();

			for (int i = 0; i < entityList.size(); i++) {
				for (int j = i + 1; j < entityList.size(); j++) {

					Entity e1 = entityList.get(i);
					Entity e2 = entityList.get(j);

					String e1Key = e1.geteName();
					String e2Key = e2.geteName();

					String e1Surf = e1.getSurface();
					String e2Surf = e2.getSurface();

					String e1Plain = e1Key;
					String e2Plain = e2Key;

					String key = e1Plain + "\t" + e2Plain;

					if (entityRelationMap.containsKey(key)) {

						int e1St = e1.getSt();
						int e1En = e1.getEn();

						int e2St = e2.getSt();
						int e2En = e2.getEn();

						Set<String> rels = entityRelationMap.get(key);
						for (String rel : rels) {

							JSONObject question = new JSONObject();

							String def = relDef.get(rel);
							if (def == null) {
								continue;
							}
							String defNL = def;
							defNL = defNL.replace("항목 주제인", "항목 주제 (이)라는");
							defNL = defNL.replace("항목 주제", "[" + e1Key + "]");
							defNL = defNL + "은(는) " + "[" + e2Key + "] 인가요?";

							if (defNLSet.contains(defNL)) {
								continue;
							} else {
								defNLSet.add(defNL);
							}

							question.put("qid", localCount);
							question.put("nlq", defNL);
							question.put("ans", "");
							question.put("ansArr", "");
							question.put("e1St", e1St);
							question.put("e1En", e1En);
							question.put("e2St", e2St);
							question.put("e2En", e2En);
							question.put("e1", e1Key);
							question.put("e2", e2Key);
							question.put("e1surf", e1Surf);
							question.put("e2surf", e2Surf);
							question.put("rel", rel);

							questions.add(question);

							System.out.println(defNL);

							localCount++;
						}

					}
				}
			}

			if (localCount > 0) {
				paragraph.put("questions", questions);
				paragraphs.add(paragraph);
				paragraphCount++;
			}

			globalCount += localCount;
			if (globalCount > 10) {

				result.put("paragraphs", paragraphs);

				// write file
				BufferedWriter bw = Files
						.newBufferedWriter(Paths.get(root, "/task4/input_181129/" + fileCount + ".json"));
				System.out.println(fileCount);
				bw.write(JsonWriter.formatJson(result.toString()));
				bw.close();
				fileCount++;
				questionCount += globalCount;
				globalCount = 0;
				result = new JSONObject();
				paragraphs.clear();

			}

		}
		
		System.out.println("prevCount : " + prevCount);

	}

	public static String root = "data/annotated/wiki-10000-part1";

	public static void main(String[] ar) throws Exception {

		RE_from_Task2 re = new RE_from_Task2();
//		re.setInputPath(Paths.get(root, "task2/convert_181121"));
//		re.loadCorpus();
//		re.setPrevSet();
		re.setInputPath(Paths.get(root, "task2/convert_181129"));
		re.loadCorpus();
		re.loadTriples();
		re.loadRelDef();
		re.generate();
		System.out.println("paragraph count : " + paragraphCount);
		System.out.println("question count : " + questionCount);

	}

}
