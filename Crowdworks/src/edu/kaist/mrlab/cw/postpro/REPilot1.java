package edu.kaist.mrlab.cw.postpro;

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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.kaist.mrlab.cw.data.QParagraph;
import edu.kaist.mrlab.cw.data.Question;

public class REPilot1 {

	private Path inputPath;
	private ArrayList<Path> filePathList;

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

	public REPilot1 setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public Map<String, String> idTitleMap = new HashMap<>();

	public void loadWikiPageID() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/wiki_id_tbl.tsv"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String id = st.nextToken();
			String title = st.nextToken();
			idTitleMap.put(id, title);
		}
	}

	public Map<String, QParagraph> textInfoMap = new HashMap<>();

	public Map<String, Integer> trueRelCount = new HashMap<>();
	public Map<String, Integer> falseRelCount = new HashMap<>();

	private static String root = "data/annotated/pilot1/";

	public void merge() throws Exception {

		BufferedWriter bw = Files.newBufferedWriter(Paths.get(root, "task4_crowd_true.csv"));
		CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
		BufferedWriter bw2 = Files.newBufferedWriter(Paths.get(root, "id_titles.txt"));
		BufferedWriter bw3 = Files.newBufferedWriter(Paths.get(root, "task4_crowd_false.csv"));
		CSVPrinter csvPrinter2 = new CSVPrinter(bw3, CSVFormat.DEFAULT);
		BufferedWriter bw4 = Files.newBufferedWriter(Paths.get(root, "true_false_per_rel.txt"));
		Set<String> docSet = new HashSet<>();

		while ((inputPath = REPilot1.this.extractInputPath()) != null) {
			BufferedReader br = Files.newBufferedReader(inputPath);

			if (inputPath.toString().contains(".DS_Store")) {
				continue;
			}

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			System.out.println("doing: " + inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			JSONParser parser = new JSONParser();
			JSONObject result = (JSONObject) parser.parse(str.toString().trim());
			JSONArray paragraphs = (JSONArray) result.get("paragraphs");
			Iterator<JSONObject> it = paragraphs.iterator();
			while (it.hasNext()) {
				JSONObject paragraph = it.next();
				String docID = paragraph.get("wikiPageId").toString();
				String parID = paragraph.get("pIdx").toString();
				docSet.add(docID);
				JSONArray questions = (JSONArray) paragraph.get("questions");
				Iterator<JSONObject> qIt = questions.iterator();
				String plainText = paragraph.get("text").toString();
				
				while (qIt.hasNext()) {
					String mentionText = plainText.replace("<", "[").replace(">", "]").replace("]", "] ");
					boolean isCorr = true;
					JSONObject question = qIt.next();
//					JSONArray ansArr = (JSONArray) question.get("ansArr");
					String ans = question.get("ans").toString();
					String e1 = question.get("e1").toString();
					String e2 = question.get("e2").toString();
					String rel = question.get("rel").toString();
					
					e1 = e1.substring(1, e1.length() - 1);
					e2 = e2.substring(1, e2.length() - 1);
					
					mentionText = mentionText.replaceFirst("\\[" + e1 + "\\]", "<e1>" + e1 + "</e1>");
					mentionText = mentionText.replaceFirst("\\[" + e2 + "\\]", "<e2>" + e2 + "</e2>");

					if(!mentionText.contains("<e1>") || !mentionText.contains("<e2>")) {
						continue;
					}
					
//					for (int i = 0; i < 3; i++) {
//						String tmp = ansArr.get(i).toString();
//						if (!ans.equals(tmp)) {
//							isCorr = false;
//							break;
//						}
//					}

					if (ans.equals("T")) {

						if (trueRelCount.containsKey(rel)) {
							int count = trueRelCount.get(rel);
							count++;
							trueRelCount.put(rel, count);
						} else {
							trueRelCount.put(rel, 1);
						}

					} else {

						if (falseRelCount.containsKey(rel)) {
							int count = falseRelCount.get(rel);
							count++;
							falseRelCount.put(rel, count);
						} else {
							falseRelCount.put(rel, 1);
						}

					}

//					if (isCorr) {
					if (ans.equals("T")) {
//						bw.write(plainText + "\t" + nlq + "\t" + ans + "\t" + ansArr + "\t" + e1 + "\t" + e2 + "\t"
//								+ rel + "\t" + e1St + "\t" + e1En + "\t" + e2St + "\t" + e2En + "\n");
						csvPrinter.printRecord(mentionText.replace("[.<line>.]", " "), rel, docID, parID, 0);
					} else {
//						bw3.write(plainText + "\t" + nlq + "\t" + ans + "\t" + ansArr + "\t" + e1 + "\t" + e2 + "\t"
//								+ rel + "\t" + e1St + "\t" + e1En + "\t" + e2St + "\t" + e2En + "\n");
						csvPrinter.printRecord(mentionText.replace("[.<line>.]", " "), rel, docID, parID, 0);
					}
				}
			}

		}

		for (String docID : docSet) {
			if (idTitleMap.containsKey(docID)) {
				bw2.write(docID + "\t" + idTitleMap.get(docID) + "\n");
			}
		}

		for (String rel : trueRelCount.keySet()) {
			bw4.write(rel + "\t" + trueRelCount.get(rel) + "\t" + falseRelCount.get(rel) + "\n");
		}

		bw.close();
		bw2.close();
		bw3.close();
		bw4.close();
		csvPrinter.close();
		csvPrinter2.close();

	}

	public static void main(String[] ar) throws Exception {

		REPilot1 re = new REPilot1();

//		re.setInputPath(Paths.get(root, "input_181123/"));
//		re.loadCorpus();
//		re.loadTextInfo();
		re.setInputPath(Paths.get(root, "task4_RE"));
		re.loadCorpus();
		re.loadWikiPageID();
		re.merge();

	}
}
