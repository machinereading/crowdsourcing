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

public class RE {

	private Path inputPath;
	private ArrayList<Path> filePathList;

	public Map<String, Integer> trueRelCount = new HashMap<>();
	public Map<String, Integer> falseRelCount = new HashMap<>();

	private static String root = "data/annotated/news-10000-part1/task4/";
	private static String date = "190311";

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

	public RE setInputPath(Path inputPath) {
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

	public void loadTextInfo() throws Exception {
		BufferedReader br = null;
		while ((inputPath = RE.this.extractInputPath()) != null) {
			br = Files.newBufferedReader(inputPath);

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
			Iterator<JSONObject> paraIter = paragraphs.iterator();

			while (paraIter.hasNext()) {
				List<Question> questionsList = new ArrayList<Question>();
				JSONObject paragraph = paraIter.next();

				int wikiPageId = Integer.parseInt(paragraph.get("docID").toString());
				int pIdx = Integer.parseInt(paragraph.get("parID").toString());
				String mentionText = paragraph.get("mentionText").toString();

				JSONArray questions = (JSONArray) paragraph.get("questions");
				Iterator<JSONObject> questionIter = questions.iterator();
				while (questionIter.hasNext()) {
					JSONObject question = questionIter.next();

					int qid = Integer.parseInt(question.get("qid").toString());
					String e1surf = question.get("e1surf").toString();
					String e2surf = question.get("e2surf").toString();

					questionsList.add(new Question(qid, e1surf, e2surf));
				}

				String key = wikiPageId + "\t" + pIdx;

				QParagraph par = new QParagraph(wikiPageId, pIdx, mentionText);
				par.questions = questionsList;

				textInfoMap.put(key, par);
			}

		}
	}

	public void merge() throws Exception {

		BufferedWriter bw = Files.newBufferedWriter(Paths.get(root, "task4_crowd_true_" + date + ".csv"));
		CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.DEFAULT);
		BufferedWriter bw2 = Files.newBufferedWriter(Paths.get(root, "id_titles.txt"));
		BufferedWriter bw3 = Files.newBufferedWriter(Paths.get(root, "task4_crowd_false_" + date + ".csv"));
		CSVPrinter csvPrinter2 = new CSVPrinter(bw3, CSVFormat.DEFAULT);
		BufferedWriter bw4 = Files.newBufferedWriter(Paths.get(root, "true_false_per_rel_" + date + ".txt"));
		Set<String> docSet = new HashSet<>();

		while ((inputPath = RE.this.extractInputPath()) != null) {
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
				String docID = paragraph.get("docID").toString();
				String parID = paragraph.get("parID").toString();
				String taggedText = paragraph.get("taggedText").toString();
				/**
				 * from co-reference --> use mentionText not taggedText
				 */
				String mentionText = paragraph.get("mentionText").toString();
				
				String key = docID + "\t" + parID;
				docSet.add(docID);
				JSONArray questions = (JSONArray) paragraph.get("questions");
				Iterator<JSONObject> qIt = questions.iterator();
				String plainText = paragraph.get("plainText").toString();

//				QParagraph qpara = textInfoMap.get(key);
//				System.out.println(key);
//				List<Question> questionList = qpara.questions;

				while (qIt.hasNext()) {
//					String mentionText = qpara.getMentionText();
					String tempText = taggedText;
					boolean isCorr = true;
					JSONObject question = qIt.next();
//					JSONArray ansArr = (JSONArray) question.get("ansArr");
					String ans = question.get("ans").toString();
					String nlq = question.get("nlq").toString();
					String e1 = question.get("e1").toString();
					String e2 = question.get("e2").toString();
					String rel = question.get("rel").toString();
					int e1St = Integer.parseInt(question.get("e1St").toString());
					int e1En = Integer.parseInt(question.get("e1En").toString());
					int e2St = Integer.parseInt(question.get("e2St").toString());
					int e2En = Integer.parseInt(question.get("e2En").toString());

					int qid = Integer.parseInt(question.get("qid").toString());
					
					if(e1.equals(e2)) {
						continue;
					}
					
//					System.out.println(tempText + "\t" + e1 + "\t" + e2);

					tempText = tempText.replace("[" + e1 + "]", "<e1>" + e1 + "</e1>");
					tempText = tempText.replace("[" + e2 + "]", "<e2>" + e2 + "</e2>");

//					Question q = questionList.get(qid);
//					String e1surf = q.getE1surf().replace("_", " ");
//					String e2surf = q.getE2surf().replace("_", " ");

//					String e1surf = valueArr[1];
//					String e2surf = valueArr[2];
//					if (mentionText == null) {
//						continue;
//					}
//					mentionText = mentionText.replaceFirst("\\[" + e1surf + "\\]", "<e1>" + e1surf + "</e1>");
//					mentionText = mentionText.replaceFirst("\\[" + e2surf + "\\]", "<e2>" + e2surf + "</e2>");
//
//					if(!mentionText.contains("<e1>") || !mentionText.contains("<e2>")) {
//						continue;
//					}

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

//					tempText = getSentenceLevelData(tempText);
//
//					if (tempText == null) {
//						continue;
//					}

//					if (isCorr) {
					if (ans.equals("T")) {
//						bw.write(plainText + "\t" + nlq + "\t" + ans + "\t" + ansArr + "\t" + e1 + "\t" + e2 + "\t"
//								+ rel + "\t" + e1St + "\t" + e1En + "\t" + e2St + "\t" + e2En + "\n");
						csvPrinter.printRecord(tempText, rel, docID, parID, 0);
//						csvPrinter.printRecord(mentionText.replace("[.<line>.]", " "), rel, docID, parID, 0);
					} else {
//						bw3.write(plainText + "\t" + nlq + "\t" + ans + "\t" + ansArr + "\t" + e1 + "\t" + e2 + "\t"
//								+ rel + "\t" + e1St + "\t" + e1En + "\t" + e2St + "\t" + e2En + "\n");
						csvPrinter2.printRecord(tempText, rel, docID, parID, 0);
//						csvPrinter2.printRecord(mentionText.replace("[.<line>.]", " "), rel, docID, parID, 0);
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

	public String getSentenceLevelData(String tempText) {

		boolean isSep = false;

		List<String> lineArr = new ArrayList<>();

		int idx = 0;
		int cut = tempText.indexOf("[.<line>.]", idx);

		while (cut > 0) {
			isSep = true;
			lineArr.add(tempText.substring(idx, cut));
			idx = cut + 1;
			cut = tempText.indexOf("[.<line>.]", idx);
		}

		lineArr.add(tempText.substring(idx, tempText.length()));

		for (String line : lineArr) {
			if (line.contains("<e1>") && line.contains("<e2>") && line.contains("</e1>") && line.contains("</e2>")) {
				return line.replace(".<line>.]", "");
			}
		}

		return null;
	}

	public static void main(String[] ar) throws Exception {

		RE re = new RE();

//		re.setInputPath(Paths.get(root, "input_181203/"));
//		re.loadCorpus();
//		re.loadTextInfo();
		re.setInputPath(Paths.get(root, "output_" + date));
		re.loadCorpus();
		re.loadWikiPageID();
		re.merge();

	}
}
