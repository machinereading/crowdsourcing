package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class QA {

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

	public QA setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}
	
	public void generate() throws Exception {
		BufferedReader br = null;
		while ((inputPath = QA.this.extractInputPath()) != null) {
			
			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			System.out.println("doing: " + inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);
			
			
			
		}
	}

	public static void main(String[] ar) throws Exception {
		QA qa = new QA();
		qa.setInputPath(Paths.get("data/annotated/pilot1/task4_RE/"));
		qa.generate();
	}
}
