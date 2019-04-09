package edu.kaist.mrlab.cw.postpro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RE2EL {

	static String root = "data/annotated/wiki-10000-part2";
	private Path inputPath;
	private ArrayList<Path> filePathList;

	Set<String> fileSet = new HashSet<>();

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

	public RE2EL setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public void loadPrevSet() throws Exception {
		BufferedReader br = Files.newBufferedReader(Paths.get("data/target/target_paragraph_list_part1.txt"));
		String input = null;
		
		while((input = br.readLine()) != null) {
			fileSet.add(input);
		}
		br.close();
	}

	public void extract() throws Exception {

		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/target/target_first_paragraph_list_from_re.txt"));
		BufferedWriter bw2 = Files.newBufferedWriter(Paths.get("data/target/removed_target_first_paragraph_list_from_re.txt"));
		BufferedReader br = null;
		while ((inputPath = RE2EL.this.extractInputPath()) != null) {

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(str.toString().trim());
			JSONArray paragraphs = (JSONArray) object.get("paragraphs");

			Iterator<JSONObject> paragraphIter = paragraphs.iterator();
			while (paragraphIter.hasNext()) {
				JSONObject paragraph = paragraphIter.next();
				int docID = Integer.parseInt(paragraph.get("docID").toString());
				int parID = Integer.parseInt(paragraph.get("parID").toString());

				String target = docID + "\t" + parID;
				
				System.out.println(target);
				
				if(!fileSet.contains(target)) {
					bw.write(target + "\n");
				} else {
					bw2.write(target + "\n");
				}

			}

		}

		bw.close();
		bw2.close();

	}

	public static void main(String[] ar) throws Exception {

		RE2EL r2e = new RE2EL();
		r2e.setInputPath(Paths.get(root, "task4/output_181217"));
		r2e.loadCorpus();
		r2e.loadPrevSet();
		r2e.extract();

	}

}
