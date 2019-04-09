package edu.kaist.mrlab.cw.postpro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FileMerge {

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

	public FileMerge setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public void merge() throws Exception {
		BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/annotated/task1_all"));

		while ((inputPath = FileMerge.this.extractInputPath()) != null) {
			BufferedReader br = Files.newBufferedReader(inputPath);

			String input = null;

			if (inputPath.toString().contains(".DS_Store")) {
				continue;
			}

			while ((input = br.readLine()) != null) {
				bw.write(input + "\n");
			}
		}
		
		bw.close();

	}

	public static void main(String[] ar) throws Exception {

		FileMerge fm = new FileMerge();
		fm.setInputPath(Paths.get("data/annotated/task1/"));
		fm.loadCorpus();
		fm.merge();

	}
}
