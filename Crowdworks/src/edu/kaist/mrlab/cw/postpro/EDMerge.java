package edu.kaist.mrlab.cw.postpro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.cedarsoftware.util.io.JsonWriter;

import edu.kaist.mrlab.cw.script.JSONArrayComparator;

public class EDMerge {

	private JSONParser mJsonParser = new JSONParser();

	private Path inputPath;
	private Path outputPath;
	private ArrayList<Path> filePathList;
	private Set<String> exceptFileSet;

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

	public EDMerge setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public EDMerge setOutputPath(Path outputPath) {
		this.outputPath = outputPath;
		return this;
	}

	private void merge() throws Exception {

		int remainCount = 0;
		int exceptCount = 0;
		BufferedReader br = null;
		while ((inputPath = EDMerge.this.extractInputPath()) != null) {

			String fileName = inputPath.getFileName().toString();

			if (fileName.contains("DS_Store")) {
				continue;
			}

			String arr[] = fileName.split("_");
			String docParID = arr[1] + "_" + arr[2];
			
//			if(exceptFileSet.contains(docParID)) {
//				System.out.println(docParID);
//				exceptCount++;
//				continue;
//			}
			
			String mulID = inputPath.getParent().toString() + "/" + (Integer.parseInt(arr[0]) + 1) + "_" + docParID;
			String mulID2 = inputPath.getParent().toString() + "/" + (Integer.parseInt(arr[0]) - 1) + "_" + docParID;

			if (filePathList.contains(Paths.get(mulID)) || filePathList.contains(Paths.get(mulID2))) {
				StringBuilder str = new StringBuilder();

				File f = new File(inputPath.toString());
				char[] c = new char[(int) f.length()];

//				System.out.println("doing: " + inputPath.toString());

				br = new BufferedReader(new FileReader(f));
				br.read(c);
				str.append(c);

				String firstWorker = str.toString();

				str = new StringBuilder();
				
				if(filePathList.contains(Paths.get(mulID2))) {
					mulID = mulID2;
				}

				f = new File(mulID.toString());
				c = new char[(int) f.length()];

//				System.out.println("doing: " + mulID.toString());

				br = new BufferedReader(new FileReader(f));
				br.read(c);
				str.append(c);

				String secondWorker = str.toString();

				writeMergeFile(firstWorker, secondWorker, docParID);
			} else {
//				System.out.println(fileName);
				remainCount++;
				continue;
			}

		}

		System.out.println(remainCount);
		System.out.println(exceptCount);
	}

	public void writeMergeFile(String firstWorker, String secondWorker, String docParID) throws Exception {

		BufferedWriter bw = Files.newBufferedWriter(Paths.get(outputPath.toString(), docParID));

		JSONObject result = new JSONObject();

		JSONObject object = (JSONObject) mJsonParser.parse(firstWorker.trim());
		JSONArray addLabel = (JSONArray) object.get("addLabel");

		int docID = Integer.parseInt(object.get("docID").toString());
		int parID = Integer.parseInt(object.get("parID").toString());
		JSONArray entities = (JSONArray) object.get("entities");
		JSONArray delLabel = (JSONArray) object.get("delLabel");
		String globalSID = object.get("globalSID").toString();
		String plainText = object.get("plainText").toString();

		Iterator<JSONObject> it = addLabel.iterator();

		JSONArray newAddLabel = new JSONArray();

		while (it.hasNext()) {
			JSONObject entity = it.next();
			if (!newAddLabel.contains(entity)) {
				newAddLabel.add(entity);
			}
		}

		object = (JSONObject) mJsonParser.parse(secondWorker.trim());
		addLabel = (JSONArray) object.get("addLabel");
		it = addLabel.iterator();
		while (it.hasNext()) {
			boolean isAddable = true;
			JSONObject entity = it.next();
			int startPosition = Integer.parseInt(entity.get("startPosition").toString());
			int endPosition = Integer.parseInt(entity.get("endPosition").toString());

			JSONArray tempAddLabel = new JSONArray();
			tempAddLabel.addAll(newAddLabel);

			Iterator<JSONObject> it2 = tempAddLabel.iterator();
			while (it2.hasNext()) {
				JSONObject entity2 = it2.next();
				int startPosition2 = Integer.parseInt(entity2.get("startPosition").toString());
				int endPosition2 = Integer.parseInt(entity2.get("endPosition").toString());
				if (startPosition == startPosition2 && endPosition == endPosition2) {
					isAddable = false;
					break;
				}
			}
			if (isAddable) {
				newAddLabel.add(entity);
			}
		}

		Collections.sort(newAddLabel, new JSONArrayComparator());

		result.put("docID", docID);
		result.put("parID", parID);
		result.put("entities", entities);
		result.put("addLabel", newAddLabel);
		result.put("delLabel", delLabel);
		result.put("globalSID", globalSID);
		result.put("plainText", plainText);

		bw.write(JsonWriter.formatJson(result.toString()));

		bw.close();

	}
	
	public void loadExceptFileSet() {
		exceptFileSet = new HashSet<>();
		while ((inputPath = EDMerge.this.extractInputPath()) != null) {

			String fileName = inputPath.getFileName().toString();

			if (fileName.contains("DS_Store")) {
				continue;
			}

			exceptFileSet.add(fileName);
		}
		
	}
	
	static String root = "data/annotated/wiki-10000-part2/task1";

	public static void main(String[] ar) throws Exception {

		EDMerge edm = new EDMerge();
		
//		edm.setInputPath(Paths.get(root, "merge_181031"));
//		edm.loadCorpus();
//		edm.loadExceptFileSet();
		
		edm.setInputPath(Paths.get(root, "output_190321"));
		edm.setOutputPath(Paths.get(root, "merge_190321"));
		edm.loadCorpus();
		edm.merge();

	}
}
